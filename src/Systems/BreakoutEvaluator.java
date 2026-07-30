package Systems;

import MetricCalculator.SignalMetrics;

import java.util.Locale;

public final class BreakoutEvaluator {

    public static final int SYSTEM_NUMBER = 2;
    public static final String SYSTEM_NAME = "Momentum Breakout / Breakdown";
    public static final double MIN_RELATIVE_VOLUME = 1.20;
    public static final double BUY_RSI_MAX = 80.0;
    public static final double SHORT_RSI_MIN = 20.0;
    public static final double STOP_ATR_MULTIPLIER = 1.0;

    private BreakoutEvaluator() {}

    public static SystemSignal evaluate(SignalMetrics metrics)
    {
        if (metrics == null)
        {
            throw new IllegalArgumentException("Signal metrics cannot be null");
        }

        boolean bullishBreakout = hasBullishBreakout(metrics);
        boolean bearishBreakdown = hasBearishBreakdown(metrics);

        if (bullishBreakout && bearishBreakdown)
        {
            throw new IllegalStateException("System 2 produced both BUY and SHORT");
        }

        if (bullishBreakout)
        {
            double stopPrice = calculateLongStop(metrics);

            return SystemSignal.active(SYSTEM_NUMBER, SYSTEM_NAME, SignalDirection.BUY, stopPrice, buildBuyReason(metrics));
        }

        if (bearishBreakdown)
        {
            double stopPrice = calculateShortStop(metrics);

            return SystemSignal.active(SYSTEM_NUMBER, SYSTEM_NAME, SignalDirection.SHORT, stopPrice, buildShortReason(metrics));
        }

        return SystemSignal.neutral(SYSTEM_NUMBER, SYSTEM_NAME, buildNeutralReason(metrics));
    }

    private static boolean hasBullishBreakout(SignalMetrics metrics)
    {
        return metrics.close() > metrics.previousHighestHigh20()
                && metrics.close() > metrics.sma20()
                && metrics.relativeVolume20() >= MIN_RELATIVE_VOLUME
                && metrics.rsi14() < BUY_RSI_MAX;
    }

    private static boolean hasBearishBreakdown(SignalMetrics metrics)
    {
        return metrics.close() < metrics.previousLowestLow20()
                && metrics.close() < metrics.sma20()
                && metrics.relativeVolume20() >= MIN_RELATIVE_VOLUME
                && metrics.rsi14() > SHORT_RSI_MIN;
    }

    private static double calculateLongStop(SignalMetrics metrics)
    {
        double rawStop = metrics.previousHighestHigh20() - STOP_ATR_MULTIPLIER * metrics.atr14();

        return validateLongStop(rawStop, metrics.close());
    }


    private static double calculateShortStop(SignalMetrics metrics)
    {
        double rawStop = metrics.previousLowestLow20() + STOP_ATR_MULTIPLIER * metrics.atr14();

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
        double breakoutPercent = calculatePercentAboveLevel(metrics.close(), metrics.previousHighestHigh20());

        return String.format(
                Locale.US,
                "Bullish 20-day breakout: close %.2f is "
                        + "%.2f%% above the previous high %.2f; "
                        + "relative volume %.2f; RSI %.2f",
                metrics.close(),
                breakoutPercent,
                metrics.previousHighestHigh20(),
                metrics.relativeVolume20(),
                metrics.rsi14()
        );
    }

    private static String buildShortReason(SignalMetrics metrics)
    {
        double breakdownPercent = calculatePercentBelowLevel(metrics.close(), metrics.previousLowestLow20());

        return String.format(
                Locale.US,
                "Bearish 20-day breakdown: close %.2f is "
                        + "%.2f%% below the previous low %.2f; "
                        + "relative volume %.2f; RSI %.2f",
                metrics.close(),
                breakdownPercent,
                metrics.previousLowestLow20(),
                metrics.relativeVolume20(),
                metrics.rsi14()
        );
    }

    private static String buildNeutralReason(SignalMetrics metrics)
    {
        boolean priceAboveHigh = metrics.close() > metrics.previousHighestHigh20();

        boolean priceBelowLow = metrics.close() < metrics.previousLowestLow20();

        boolean sufficientVolume = metrics.relativeVolume20() >= MIN_RELATIVE_VOLUME;

        if (priceAboveHigh && !sufficientVolume)
        {
            return String.format(Locale.US,
                    "Unconfirmed bullish breakout: close exceeded "
                            + "the 20-day high, but relative volume "
                            + "%.2f was below %.2f",
                    metrics.relativeVolume20(),
                    MIN_RELATIVE_VOLUME);
        }

        if (priceBelowLow && !sufficientVolume)
        {
            return String.format(Locale.US,
                    "Unconfirmed bearish breakdown: close fell "
                            + "below the 20-day low, but relative "
                            + "volume %.2f was below %.2f",
                    metrics.relativeVolume20(),
                    MIN_RELATIVE_VOLUME);
        }

        if (priceAboveHigh && metrics.rsi14() >= BUY_RSI_MAX)
        {
            return String.format(Locale.US,
                    "Bullish breakout rejected because RSI %.2f "
                            + "was at or above %.2f",
                    metrics.rsi14(),
                    BUY_RSI_MAX);
        }

        if (priceBelowLow && metrics.rsi14() <= SHORT_RSI_MIN)
        {
            return String.format(Locale.US,
                    "Bearish breakdown rejected because RSI %.2f "
                            + "was at or below %.2f",
                    metrics.rsi14(),
                    SHORT_RSI_MIN);
        }

        return String.format(Locale.US,
                "No confirmed 20-day breakout. Close %.2f, "
                        + "previous high %.2f, previous low %.2f, "
                        + "relative volume %.2f",
                metrics.close(),
                metrics.previousHighestHigh20(),
                metrics.previousLowestLow20(),
                metrics.relativeVolume20());
    }

    private static double calculatePercentAboveLevel(double price, double level)
    {
        if (level <= 0.0)
        {
            return 0.0;
        }

        return (price - level) / level * 100.0;
    }

    private static double calculatePercentBelowLevel(double price, double level)
    {
        if (level <= 0.0)
        {
            return 0.0;
        }

        return (level - price) / level * 100.0;
    }
}