package GUI;

import MetricCalculator.SignalMetrics;
import SignalEvaluator.StockSignalResult;
import Systems.SystemSignal;

import java.time.LocalDate;

/**
 * One display row for the System 2 page.
 *
 * This record does not calculate indicators and does not evaluate
 * breakouts. It only converts one StockSignalResult into values
 * needed by the JavaFX table.
 */
public record System2Row(
        String ticker,
        LocalDate date,

        String status,
        String rawDirection,

        double price,
        double sma20,

        double previousHighestHigh20,
        double previousLowestLow20,

        double relativeVolume20,
        double rsi14,
        double atr14,

        double stopPrice,
        String reason
) {
    public System2Row {
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

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Status cannot be blank"
            );
        }

        if (rawDirection == null
                || rawDirection.isBlank()) {
            throw new IllegalArgumentException(
                    "Raw direction cannot be blank"
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
     * One GUI-ready System2Row.
     */
    public static System2Row from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        SignalMetrics metrics =
                result.metrics();

        SystemSignal system2 =
                result.system2();

        String displayStatus;

        if (system2.muted()) {
            displayStatus = "MUTED";
        } else {
            displayStatus =
                    system2.finalDirection().name();
        }

        return new System2Row(
                result.ticker(),
                result.date(),

                displayStatus,
                system2.rawDirection().name(),

                metrics.close(),
                metrics.sma20(),

                metrics.previousHighestHigh20(),
                metrics.previousLowestLow20(),

                metrics.relativeVolume20(),
                metrics.rsi14(),
                metrics.atr14(),

                system2.stopPrice(),
                system2.reason()
        );
    }

    public boolean hasActiveSignal() {
        return status.equals("BUY")
                || status.equals("SHORT");
    }

    public boolean isMuted() {
        return status.equals("MUTED");
    }

    public boolean isNeutral() {
        return status.equals("NEUTRAL");
    }

    /**
     * Positive means price is above the previous 20-day high.
     */
    public double percentAboveHigh() {
        if (previousHighestHigh20 == 0.0) {
            return 0.0;
        }

        return (price - previousHighestHigh20)
                / previousHighestHigh20
                * 100.0;
    }

    /**
     * Positive means price is below the previous 20-day low.
     */
    public double percentBelowLow() {
        if (previousLowestLow20 == 0.0) {
            return 0.0;
        }

        return (previousLowestLow20 - price)
                / previousLowestLow20
                * 100.0;
    }
}