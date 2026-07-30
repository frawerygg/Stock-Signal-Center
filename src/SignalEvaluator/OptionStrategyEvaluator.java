package SignalEvaluator;

import MetricCalculator.SignalMetrics;

import java.util.Locale;

public final class OptionStrategyEvaluator
{
    private static final double COVERED_CALL_POSITION_MIN = 0.60;
    private static final double COVERED_CALL_POSITION_MAX = 0.95;
    private static final double CASH_PUT_POSITION_MIN = 0.05;
    private static final double CASH_PUT_POSITION_MAX = 0.40;

    private static final double COVERED_CALL_RSI_MIN = 50.0;
    private static final double COVERED_CALL_RSI_MAX = 65.0;
    private static final double CASH_PUT_RSI_MIN = 35.0;
    private static final double CASH_PUT_RSI_MAX = 50.0;

    private static final double MAX_RELATIVE_VOLUME = 1.20;

    /*
     * Covered call:
     * Allow a flat or mildly rising SMA20.
     *
     * The stock should not be falling meaningfully, but a small negative
     * five-day slope is acceptable in an otherwise range-bound market.
     */
    private static final double COVERED_CALL_SMA20_SLOPE_MIN = -0.25;
    private static final double COVERED_CALL_SMA20_SLOPE_MAX = 0.75;
    private static final double COVERED_CALL_MA_DISTANCE_MAX = 1.50;

    /*
     * Cash-secured put:
     * Require a flat or mildly rising SMA20.
     *
     * This avoids selling puts on a stock whose short-term trend is
     * actively deteriorating.
     */
    private static final double CASH_PUT_SMA20_SLOPE_MIN = 0.00;
    private static final double CASH_PUT_SMA20_SLOPE_MAX = 0.60;
    private static final double CASH_PUT_MA_DISTANCE_MAX = 1.50;

    /*
     * Signed SMA relationship:
     *
     * -0.50 means SMA20 may be at most 0.50% below SMA50.
     */
    private static final double CASH_PUT_SMA20_VS_SMA50_MIN = -0.50;

    private OptionStrategyEvaluator()
    {}

    public static OptionStrategyEvaluation evaluate(
            SignalMetrics metrics,
            SignalResolution resolution)
    {
        validateInputs(metrics, resolution);

        MarketRegime regime = resolution.marketRegime();

        if (regime == MarketRegime.COMPRESSION)
        {
            return new OptionStrategyEvaluation(
                    OptionStrategySignal.BREAKOUT_RISK,
                    "Volatility compression detected; avoid selling options "
                            + "until the next directional move becomes clearer"
            );
        }

        if (regime == MarketRegime.FALSE_BREAKOUT_UP
                || regime == MarketRegime.FALSE_BREAKOUT_DOWN)
        {
            return noSignal(
                    "No options-income candidate during a false breakout"
            );
        }

        if (regime != MarketRegime.RANGE_BOUND)
        {
            return noSignal(
                    "Market is not currently classified as range-bound"
            );
        }

        if (resolution.hasAnyDirectionalSignal())
        {
            return noSignal(
                    "An active directional signal overrides the "
                            + "options-income candidate"
            );
        }

        double bollingerPosition = calculateBollingerPosition(metrics);

        if (isCoveredCallCandidate(metrics, bollingerPosition))
        {
            return new OptionStrategyEvaluation(
                    OptionStrategySignal.COVERED_CALL_CANDIDATE,
                    buildCoveredCallExplanation(
                            metrics,
                            bollingerPosition
                    )
            );
        }

        if (isCashSecuredPutCandidate(metrics, bollingerPosition))
        {
            return new OptionStrategyEvaluation(
                    OptionStrategySignal.CASH_SECURED_PUT_CANDIDATE,
                    buildCashPutExplanation(
                            metrics,
                            bollingerPosition
                    )
            );
        }

        return noSignal(
                buildNoSignalExplanation(metrics, bollingerPosition)
        );
    }

    private static boolean isCoveredCallCandidate(
            SignalMetrics metrics,
            double bollingerPosition)
    {
        return hasCoveredCallMovingAverageStructure(metrics)
                && bollingerPosition >= COVERED_CALL_POSITION_MIN
                && bollingerPosition <= COVERED_CALL_POSITION_MAX
                && metrics.close() >= metrics.sma20()
                && metrics.rsi14() >= COVERED_CALL_RSI_MIN
                && metrics.rsi14() <= COVERED_CALL_RSI_MAX
                && metrics.relativeVolume20() <= MAX_RELATIVE_VOLUME;
    }

    private static boolean isCashSecuredPutCandidate(
            SignalMetrics metrics,
            double bollingerPosition)
    {
        return hasCashPutMovingAverageStructure(metrics)
                && bollingerPosition >= CASH_PUT_POSITION_MIN
                && bollingerPosition <= CASH_PUT_POSITION_MAX
                && metrics.close() <= metrics.sma20()
                && metrics.rsi14() >= CASH_PUT_RSI_MIN
                && metrics.rsi14() <= CASH_PUT_RSI_MAX
                && metrics.relativeVolume20() <= MAX_RELATIVE_VOLUME;
    }

    private static boolean hasCoveredCallMovingAverageStructure(
            SignalMetrics metrics)
    {
        double sma20Slope = metrics.sma20SlopePercent();

        return sma20Slope >= COVERED_CALL_SMA20_SLOPE_MIN
                && sma20Slope <= COVERED_CALL_SMA20_SLOPE_MAX
                && metrics.movingAverageDistancePercent()
                <= COVERED_CALL_MA_DISTANCE_MAX;
    }

    private static boolean hasCashPutMovingAverageStructure(
            SignalMetrics metrics)
    {
        double sma20Slope = metrics.sma20SlopePercent();
        double sma20VsSma50 =
                calculateSma20VsSma50Percent(metrics);

        return sma20Slope >= CASH_PUT_SMA20_SLOPE_MIN
                && sma20Slope <= CASH_PUT_SMA20_SLOPE_MAX
                && metrics.movingAverageDistancePercent()
                <= CASH_PUT_MA_DISTANCE_MAX
                && sma20VsSma50
                >= CASH_PUT_SMA20_VS_SMA50_MIN;
    }

    /*
     * Returns a signed percentage.
     *
     * Positive:
     * SMA20 is above SMA50.
     *
     * Negative:
     * SMA20 is below SMA50.
     */
    private static double calculateSma20VsSma50Percent(
            SignalMetrics metrics)
    {
        return (metrics.sma20() - metrics.sma50())
                / metrics.close()
                * 100.0;
    }

    private static double calculateBollingerPosition(
            SignalMetrics metrics)
    {
        double bandRange =
                metrics.bollingerUpper20()
                        - metrics.bollingerLower20();

        if (bandRange <= 0.0)
        {
            return 0.50;
        }

        return (metrics.close() - metrics.bollingerLower20())
                / bandRange;
    }

    private static String buildCoveredCallExplanation(
            SignalMetrics metrics,
            double bollingerPosition)
    {
        return String.format(
                Locale.US,
                "Range-bound covered-call candidate: Bollinger position "
                        + "%.0f%%, RSI %.2f, relative volume %.2f, "
                        + "five-day SMA20 slope %.2f%%, and SMA20/SMA50 "
                        + "distance %.2f%%",
                bollingerPosition * 100.0,
                metrics.rsi14(),
                metrics.relativeVolume20(),
                metrics.sma20SlopePercent(),
                metrics.movingAverageDistancePercent()
        );
    }

    private static String buildCashPutExplanation(
            SignalMetrics metrics,
            double bollingerPosition)
    {
        return String.format(
                Locale.US,
                "Range-bound cash-secured-put candidate: Bollinger position "
                        + "%.0f%%, RSI %.2f, relative volume %.2f, "
                        + "five-day SMA20 slope %.2f%%, SMA20/SMA50 "
                        + "distance %.2f%%, and SMA20 versus SMA50 %.2f%%",
                bollingerPosition * 100.0,
                metrics.rsi14(),
                metrics.relativeVolume20(),
                metrics.sma20SlopePercent(),
                metrics.movingAverageDistancePercent(),
                calculateSma20VsSma50Percent(metrics)
        );
    }

    private static String buildNoSignalExplanation(
            SignalMetrics metrics,
            double bollingerPosition)
    {
        return String.format(
                Locale.US,
                "Market is range-bound, but Bollinger position %.0f%%, "
                        + "RSI %.2f, relative volume %.2f, five-day SMA20 "
                        + "slope %.2f%%, and SMA20/SMA50 distance %.2f%% "
                        + "do not satisfy an options strategy",
                bollingerPosition * 100.0,
                metrics.rsi14(),
                metrics.relativeVolume20(),
                metrics.sma20SlopePercent(),
                metrics.movingAverageDistancePercent()
        );
    }

    private static OptionStrategyEvaluation noSignal(
            String explanation)
    {
        return new OptionStrategyEvaluation(
                OptionStrategySignal.NO_SIGNAL,
                explanation
        );
    }

    private static void validateInputs(
            SignalMetrics metrics,
            SignalResolution resolution)
    {
        if (metrics == null)
        {
            throw new IllegalArgumentException(
                    "Signal metrics cannot be null"
            );
        }

        if (resolution == null)
        {
            throw new IllegalArgumentException(
                    "Signal resolution cannot be null"
            );
        }
    }
}