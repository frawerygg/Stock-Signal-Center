package GUI;

import MetricCalculator.SignalMetrics;
import SignalEvaluator.StockSignalResult;
import Systems.SystemSignal;

import java.time.LocalDate;

/**
 * One row displayed on the System 1 page.
 *
 * This record does not calculate indicators or evaluate signals.
 * It only extracts System 1 information from StockSignalResult.
 */
public record System1Row(
        String ticker,
        LocalDate date,

        String status,
        String rawDirection,

        double price,
        double previousClose,

        double previousSma20,
        double previousSma50,

        double sma20,
        double sma50,
        double sma20FiveDaysAgo,

        double rsi14,
        double atr14,
        double stopPrice,

        String reason
) {
    public System1Row {
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

        if (rawDirection == null || rawDirection.isBlank()) {
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
     * One GUI-ready System1Row.
     */
    public static System1Row from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        SignalMetrics metrics =
                result.metrics();

        SystemSignal system1 =
                result.system1();

        String displayStatus;

        if (system1.muted()) {
            displayStatus = "MUTED";
        } else {
            displayStatus =
                    system1.finalDirection().name();
        }

        return new System1Row(
                result.ticker(),
                result.date(),

                displayStatus,
                system1.rawDirection().name(),

                metrics.close(),
                metrics.previousClose(),

                metrics.previousSma20(),
                metrics.previousSma50(),

                metrics.sma20(),
                metrics.sma50(),
                metrics.sma20FiveDaysAgo(),

                metrics.rsi14(),
                metrics.atr14(),
                system1.stopPrice(),

                system1.reason()
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
}