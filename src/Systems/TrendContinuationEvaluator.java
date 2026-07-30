package Systems;

import MetricCalculator.SignalMetrics;

import java.util.Locale;


public final class TrendContinuationEvaluator
{
    public static final int SYSTEM_NUMBER = 1;
    public static final String SYSTEM_NAME = "Trend Continuation";
    private static final double BUY_RSI_MIN = 50.0;
    private static final double BUY_RSI_MAX = 70.0;
    private static final double SHORT_RSI_MIN = 30.0;
    private static final double SHORT_RSI_MAX = 50.0;
    private static final double STOP_ATR_MULTIPLIER = 0.5;

    private TrendContinuationEvaluator() {}

    public static SystemSignal evaluate(SignalMetrics metrics)
    {
        if (metrics == null)
        {
            throw new IllegalArgumentException("Signal metrics cannot be null");
        }

        boolean buyConditions = hasBullishTrend(metrics);
        boolean shortConditions = hasBearishTrend(metrics);

        if (buyConditions && shortConditions)
        {
            throw new IllegalStateException("System 1 produced both BUY and SHORT");
        }

        if (buyConditions)
        {
            double stopPrice = calculateLongStop(metrics);
            return SystemSignal.active(SYSTEM_NUMBER, SYSTEM_NAME, SignalDirection.BUY, stopPrice, buildBuyReason(metrics));
        }

        if (shortConditions)
        {
            double stopPrice = calculateShortStop(metrics);
            return SystemSignal.active(SYSTEM_NUMBER, SYSTEM_NAME, SignalDirection.SHORT, stopPrice, buildShortReason(metrics));
        }

        return SystemSignal.neutral(SYSTEM_NUMBER, SYSTEM_NAME, buildNeutralReason(metrics));
    }


    private static boolean hasBullishTrend(SignalMetrics metrics)
    {
        boolean bullishCrossover = metrics.previousSma20() <= metrics.previousSma50()
                && metrics.sma20() > metrics.sma50();

        return bullishCrossover
                && metrics.close() > metrics.sma20()
                && metrics.sma20() > metrics.sma20FiveDaysAgo()
                && metrics.close() > metrics.previousClose()
                && metrics.rsi14() >= BUY_RSI_MIN
                && metrics.rsi14() <= BUY_RSI_MAX;
    }


    private static boolean hasBearishTrend(SignalMetrics metrics)
    {
        boolean bearishCrossover = metrics.previousSma20() >= metrics.previousSma50()
                && metrics.sma20() < metrics.sma50();

        return bearishCrossover
                && metrics.close() < metrics.sma20()
                && metrics.sma20() < metrics.sma20FiveDaysAgo()
                && metrics.close() < metrics.previousClose()
                && metrics.rsi14() >= SHORT_RSI_MIN
                && metrics.rsi14() <= SHORT_RSI_MAX;
    }


    private static double calculateLongStop(SignalMetrics metrics)
    {
        double rawStop = Math.min(metrics.sma50(), metrics.previousLowestLow7()) - STOP_ATR_MULTIPLIER * metrics.atr14();

        return validateLongStop(rawStop, metrics.close());
    }


    private static double calculateShortStop(SignalMetrics metrics)
    {
        double rawStop = Math.max(metrics.sma50(), metrics.previousHighestHigh7()) + STOP_ATR_MULTIPLIER * metrics.atr14();

        return validateShortStop(rawStop, metrics.close());
    }


    private static double validateLongStop(double stopPrice, double currentPrice)
    {
        if (!Double.isFinite(stopPrice) || stopPrice <= 0.0 || stopPrice >= currentPrice)
        {
            return Double.NaN;
        }
        return stopPrice;
    }

    private static double validateShortStop(double stopPrice, double currentPrice)
    {
        if (!Double.isFinite(stopPrice) || stopPrice <= currentPrice)
        {
            return Double.NaN;
        }
        return stopPrice;
    }

    private static String buildBuyReason(SignalMetrics metrics)
    {
        return String.format(
                Locale.US,
                "Bullish SMA crossover: yesterday SMA20 %.2f was below "
                        + "SMA50 %.2f; today SMA20 %.2f is above "
                        + "SMA50 %.2f; close %.2f is above SMA20; RSI %.2f",
                metrics.previousSma20(),
                metrics.previousSma50(),
                metrics.sma20(),
                metrics.sma50(),
                metrics.close(),
                metrics.rsi14()
        );
    }

private static String buildShortReason(SignalMetrics metrics)
{
    return String.format(
            Locale.US,
            "Bearish SMA crossover: yesterday SMA20 %.2f was above "
                    + "SMA50 %.2f; today SMA20 %.2f is below "
                    + "SMA50 %.2f; close %.2f is below SMA20; RSI %.2f",
            metrics.previousSma20(),
            metrics.previousSma50(),
            metrics.sma20(),
            metrics.sma50(),
            metrics.close(),
            metrics.rsi14()
    );
}

    private static String buildNeutralReason(SignalMetrics metrics)
    {
        return String.format(Locale.US,
                "No complete trend signal. "
                        + "Close %.2f, SMA20 %.2f, "
                        + "SMA50 %.2f, RSI %.2f",
                metrics.close(),
                metrics.sma20(),
                metrics.sma50(),
                metrics.rsi14());
    }
}