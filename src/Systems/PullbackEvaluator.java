package Systems;

import MetricCalculator.SignalMetrics;

import java.util.Locale;

public final class PullbackEvaluator
{
    public static final int SYSTEM_NUMBER = 3;
    public static final String SYSTEM_NAME = "Extreme Pullback Recovery";
    private static final double MIN_DRAWDOWN_PERCENT = 20.0;
    private static final double MIN_DRAWDOWN_ATR_MULTIPLE = 4.0;
    private static final double MIN_RECOVERY_ATR_MULTIPLE = 1.0;
    private static final double RSI_RECOVERY_LEVEL = 35.0;
    private static final double STOP_ATR_MULTIPLIER = 0.5;

    private PullbackEvaluator() {}

    public static SystemSignal evaluate(SignalMetrics metrics)
    {
        validateMetrics(metrics);
        if (hasBullishRecoverySignal(metrics))
        {
            double stopPrice = calculateLongStop(metrics);
            return SystemSignal.active(SYSTEM_NUMBER, SYSTEM_NAME, SignalDirection.BUY, stopPrice, buildBuyReason(metrics));
        }
        return SystemSignal.neutral(SYSTEM_NUMBER, SYSTEM_NAME, buildNeutralReason(metrics));
    }


    private static boolean hasBullishRecoverySignal(SignalMetrics metrics)
    {
        boolean severePercentageDrawdown = metrics.pullbackDrawdownPercent21() >= MIN_DRAWDOWN_PERCENT;
        boolean severeAtrDrawdown = metrics.pullbackDrawdownAtrMultiple21() >= MIN_DRAWDOWN_ATR_MULTIPLE;
        boolean sufficientRecovery = metrics.recoveryFromTroughAtrMultiple21() >= MIN_RECOVERY_ATR_MULTIPLE;
        boolean rsiRecovered = metrics.previousRsi14() <= RSI_RECOVERY_LEVEL && metrics.rsi14() > RSI_RECOVERY_LEVEL;
        boolean priceIsRising = metrics.priceChangePercent() > 0.0;
        boolean momentumIsImproving = metrics.priceAccelerationPercent() > 0.0;

        return severePercentageDrawdown
                && severeAtrDrawdown
                && sufficientRecovery
                && rsiRecovered
                && priceIsRising
                && momentumIsImproving;
    }


    private static double calculateLongStop(
            SignalMetrics metrics)
    {
        double stopPrice = metrics.pullbackTroughLow21() - STOP_ATR_MULTIPLIER * metrics.atr14();

        if (!Double.isFinite(stopPrice) || stopPrice <= 0.0 || stopPrice >= metrics.close())
        {
            return Double.NaN;
        }
        return stopPrice;
    }


    private static String buildBuyReason(SignalMetrics metrics)
    {
        return String.format(Locale.US,
                "Extreme pullback recovery: drawdown %.2f%% "
                        + "(%.2f ATR), recovery %.2f ATR, "
                        + "RSI rose from %.2f to %.2f, "
                        + "price change %.2f%%, acceleration %.2f%%",
                metrics.pullbackDrawdownPercent21(),
                metrics.pullbackDrawdownAtrMultiple21(),
                metrics.recoveryFromTroughAtrMultiple21(),
                metrics.previousRsi14(),
                metrics.rsi14(),
                metrics.priceChangePercent(),
                metrics.priceAccelerationPercent());
    }


    private static String buildNeutralReason(
            SignalMetrics metrics)
    {
        return String.format(Locale.US,
                "No extreme pullback recovery. "
                        + "Drawdown %.2f%%, drawdown %.2f ATR, "
                        + "recovery %.2f ATR, RSI %.2f -> %.2f, "
                        + "change %.2f%%, acceleration %.2f%%",
                metrics.pullbackDrawdownPercent21(),
                metrics.pullbackDrawdownAtrMultiple21(),
                metrics.recoveryFromTroughAtrMultiple21(),
                metrics.previousRsi14(),
                metrics.rsi14(),
                metrics.priceChangePercent(),
                metrics.priceAccelerationPercent());
    }


    private static void validateMetrics(SignalMetrics metrics)
    {
        if (metrics == null)
        {
            throw new IllegalArgumentException(
                    "Signal metrics cannot be null"
            );
        }
    }
}