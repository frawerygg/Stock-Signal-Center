package GUI;

import SignalEvaluator.StockSignalResult;
import Systems.SignalDirection;
import Systems.SystemSignal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * One protective-stop row for the Exit / Sell Points page.
 *
 * One StockSignalResult may produce up to three ExitRow objects:
 * one for System 1, one for System 2, and one for System 3.
 *
 * This record does not calculate stop prices.
 * It only interprets stop prices already produced by the backend.
 */
public record ExitRow(
        String ticker,
        LocalDate date,

        int systemNumber,
        String systemName,

        String signalStatus,
        String tradeDirection,

        double currentPrice,
        double stopPrice,
        double distancePercent,

        String exitStatus,
        boolean muted,

        String reason
) {
    public ExitRow {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticker cannot be blank"
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Date cannot be null"
            );
        }

        if (systemNumber <= 0) {
            throw new IllegalArgumentException(
                    "System number must be positive"
            );
        }

        if (systemName == null || systemName.isBlank()) {
            throw new IllegalArgumentException(
                    "System name cannot be blank"
            );
        }

        if (signalStatus == null || signalStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Signal status cannot be blank"
            );
        }

        if (tradeDirection == null || tradeDirection.isBlank()) {
            throw new IllegalArgumentException(
                    "Trade direction cannot be blank"
            );
        }

        if (exitStatus == null || exitStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Exit status cannot be blank"
            );
        }

        if (reason == null) {
            reason = "";
        }
    }


    /**
     * Input:
     * One complete StockSignalResult.
     *
     * Output:
     * Zero to three ExitRow objects.
     *
     * Neutral systems and systems without a finite stop price
     * are excluded.
     */
    public static List<ExitRow> from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        List<ExitRow> rows =
                new ArrayList<>();

        addSystemRow(
                rows,
                result,
                result.system1()
        );

        addSystemRow(
                rows,
                result,
                result.system2()
        );

        addSystemRow(
                rows,
                result,
                result.system3()
        );

        return List.copyOf(rows);
    }


    private static void addSystemRow(
            List<ExitRow> rows,
            StockSignalResult result,
            SystemSignal signal
    ) {
        SignalDirection rawDirection =
                signal.rawDirection();

        /*
         * A neutral system does not represent a potential position.
         */
        if (rawDirection == SignalDirection.NEUTRAL) {
            return;
        }

        double stopPrice =
                signal.stopPrice();

        /*
         * Double.NaN means that no valid stop was calculated.
         */
        if (!Double.isFinite(stopPrice)) {
            return;
        }

        SignalDirection displayedDirection;

        if (signal.muted()) {
            /*
             * A muted signal has a final direction of NEUTRAL,
             * but its raw direction still tells us whether this
             * was originally a BUY or SHORT setup.
             */
            displayedDirection =
                    rawDirection;
        } else {
            displayedDirection =
                    signal.finalDirection();
        }

        if (displayedDirection
                == SignalDirection.NEUTRAL) {
            return;
        }

        double currentPrice =
                result.price();

        double distancePercent;

        if (currentPrice == 0.0) {
            distancePercent =
                    Double.NaN;
        } else {
            distancePercent =
                    Math.abs(
                            currentPrice - stopPrice
                    )
                            / currentPrice
                            * 100.0;
        }

        boolean stopCrossed =
                isStopCrossed(
                        displayedDirection,
                        currentPrice,
                        stopPrice
                );

        String signalStatus;

        if (signal.muted()) {
            signalStatus = "MUTED";
        } else {
            signalStatus =
                    signal.finalDirection()
                            .name();
        }

        String exitStatus;

        if (signal.muted()) {
            exitStatus =
                    "INACTIVE RAW STOP";
        } else if (stopCrossed) {
            exitStatus =
                    "EXIT TRIGGERED";
        } else {
            exitStatus =
                    "HOLD";
        }

        rows.add(
                new ExitRow(
                        result.ticker(),
                        result.date(),

                        signal.systemNumber(),
                        signal.systemName(),

                        signalStatus,
                        displayedDirection.name(),

                        currentPrice,
                        stopPrice,
                        distancePercent,

                        exitStatus,
                        signal.muted(),

                        signal.reason()
                )
        );
    }


    private static boolean isStopCrossed(
            SignalDirection direction,
            double currentPrice,
            double stopPrice
    ) {
        return switch (direction) {
            case BUY ->
                    currentPrice <= stopPrice;

            case SHORT ->
                    currentPrice >= stopPrice;

            case NEUTRAL ->
                    false;
        };
    }


    public String systemDisplayName() {
        return "S"
                + systemNumber
                + " — "
                + systemName;
    }


    public boolean isExitTriggered() {
        return exitStatus.equals(
                "EXIT TRIGGERED"
        );
    }


    public boolean isHold() {
        return exitStatus.equals(
                "HOLD"
        );
    }


    public boolean isInactiveRawStop() {
        return exitStatus.equals(
                "INACTIVE RAW STOP"
        );
    }


    public boolean isLongStop() {
        return tradeDirection.equals(
                "BUY"
        );
    }


    public boolean isShortStop() {
        return tradeDirection.equals(
                "SHORT"
        );
    }
}