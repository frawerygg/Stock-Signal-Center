package GUI;

import MetricCalculator.SignalMetrics;
import SignalEvaluator.StockSignalResult;
import Systems.SystemSignal;

import java.time.LocalDate;

/**
 * One display row for the System 3 page.
 *
 * This record does not calculate the pullback or evaluate the signal.
 * It only extracts existing System 3 values from StockSignalResult.
 */
public record System3Row(
        String ticker,
        LocalDate date,

        String status,
        String rawDirection,

        double price,
        double previousClose,

        double peakClose21,
        double peakAtr14,

        double troughClose21,
        double troughLow21,

        double drawdownPercent21,
        double drawdownAtrMultiple21,
        double recoveryAtrMultiple21,

        double previousRsi14,
        double rsi14,

        double previousPriceChangePercent,
        double priceChangePercent,
        double accelerationPercent,

        double atr14,
        double stopPrice,

        String reason
) {
    public System3Row {
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
     * One GUI-ready System3Row.
     */
    public static System3Row from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        SignalMetrics metrics =
                result.metrics();

        SystemSignal system3 =
                result.system3();

        String displayStatus;

        if (system3.muted()) {
            displayStatus = "MUTED";
        } else {
            displayStatus =
                    system3.finalDirection().name();
        }

        return new System3Row(
                result.ticker(),
                result.date(),

                displayStatus,
                system3.rawDirection().name(),

                metrics.close(),
                metrics.previousClose(),

                metrics.pullbackPeakClose21(),
                metrics.pullbackPeakAtr14(),

                metrics.pullbackTroughClose21(),
                metrics.pullbackTroughLow21(),

                metrics.pullbackDrawdownPercent21(),
                metrics.pullbackDrawdownAtrMultiple21(),
                metrics.recoveryFromTroughAtrMultiple21(),

                metrics.previousRsi14(),
                metrics.rsi14(),

                metrics.previousPriceChangePercent(),
                metrics.priceChangePercent(),
                metrics.priceAccelerationPercent(),

                metrics.atr14(),
                system3.stopPrice(),

                system3.reason()
        );
    }


    /**
     * The current PullbackEvaluator only generates BUY signals.
     */
    public boolean hasActiveSignal() {
        return status.equals("BUY");
    }


    public boolean isMuted() {
        return status.equals("MUTED");
    }


    public boolean isNeutral() {
        return status.equals("NEUTRAL");
    }


    /**
     * Shows stocks that already passed both severe-drawdown tests,
     * even when the recovery conditions have not triggered yet.
     */
    public boolean hasSeverePullback() {
        return drawdownPercent21 >= 20.0
                && drawdownAtrMultiple21 >= 4.0;
    }


    public boolean passedRecoveryDistance() {
        return recoveryAtrMultiple21 >= 1.0;
    }


    public boolean passedRsiRecovery() {
        return previousRsi14 <= 35.0
                && rsi14 > 35.0;
    }


    public boolean passedPositivePriceChange() {
        return priceChangePercent > 0.0;
    }


    public boolean passedAcceleration() {
        return accelerationPercent > 0.0;
    }
}