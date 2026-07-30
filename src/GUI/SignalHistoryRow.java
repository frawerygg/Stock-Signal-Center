package GUI;

import SignalEvaluator.MarketRegime;
import SignalEvaluator.MarketRegimeEvaluator;
import SignalEvaluator.StockSignalResult;
import Systems.SystemSignal;

import java.time.LocalDate;
import java.util.Locale;

/**
 * One event displayed on the seven-day signal-history page.
 *
 * For Systems 1–3, a row represents a directional raw signal.
 * For System 4, a row represents a market-regime change.
 */
public record SignalHistoryRow(
        String ticker,
        LocalDate date,

        int systemNumber,
        String systemName,

        String resultStatus,
        String rawOrTransition,

        double signalPrice,
        double stopPrice,

        String reason
) {
    public SignalHistoryRow {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticker cannot be blank"
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "History date cannot be null"
            );
        }

        if (systemNumber < 1 || systemNumber > 4) {
            throw new IllegalArgumentException(
                    "System number must be between 1 and 4"
            );
        }

        if (systemName == null || systemName.isBlank()) {
            throw new IllegalArgumentException(
                    "System name cannot be blank"
            );
        }

        if (resultStatus == null || resultStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Result status cannot be blank"
            );
        }

        if (rawOrTransition == null) {
            rawOrTransition = "";
        }

        if (reason == null) {
            reason = "";
        }
    }


    /**
     * Creates a history event for System 1, 2, or 3.
     *
     * Only call this when the raw direction is BUY or SHORT.
     */
    public static SignalHistoryRow fromSystem(
            StockSignalResult result,
            SystemSignal signal
    ) {
        if (result == null || signal == null) {
            throw new IllegalArgumentException(
                    "Result and system signal cannot be null"
            );
        }

        if (!signal.rawDirection().isDirectional()) {
            throw new IllegalArgumentException(
                    "Neutral signals are not history events"
            );
        }

        String displayStatus;

        if (signal.muted()) {
            displayStatus = "MUTED";
        } else {
            displayStatus =
                    signal.finalDirection().name();
        }

        return new SignalHistoryRow(
                result.ticker(),
                result.date(),

                signal.systemNumber(),
                signal.systemName(),

                displayStatus,
                signal.rawDirection().name(),

                result.price(),
                signal.stopPrice(),

                signal.reason()
        );
    }


    /**
     * Creates a System 4 row when the market regime changes.
     */
    public static SignalHistoryRow fromRegimeChange(
            StockSignalResult result,
            MarketRegime previousRegime
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        if (previousRegime == null) {
            throw new IllegalArgumentException(
                    "Previous market regime cannot be null"
            );
        }

        MarketRegime currentRegime =
                result.marketRegime();

        String transition =
                previousRegime.getDisplayName()
                        + " -> "
                        + currentRegime.getDisplayName();

        return new SignalHistoryRow(
                result.ticker(),
                result.date(),

                4,
                "Market Regime",

                currentRegime
                        .getDisplayName()
                        .toUpperCase(Locale.ROOT),

                transition,

                result.price(),
                Double.NaN,

                MarketRegimeEvaluator.getExplanation(
                        currentRegime
                )
        );
    }


    public String systemDisplayName() {
        return "S"
                + systemNumber
                + " — "
                + systemName;
    }


    public boolean isDirectionalEvent() {
        return systemNumber >= 1
                && systemNumber <= 3;
    }


    public boolean isRegimeEvent() {
        return systemNumber == 4;
    }


    public boolean isBuy() {
        return rawOrTransition.equals("BUY");
    }


    public boolean isShort() {
        return rawOrTransition.equals("SHORT");
    }


    public boolean isMuted() {
        return resultStatus.equals("MUTED");
    }
}