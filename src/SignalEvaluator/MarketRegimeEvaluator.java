package SignalEvaluator;

import Dataloader.DailyPrice;
import Dataloader.StockData;
import MetricCalculator.IndicatorCalculator;
import MetricCalculator.SignalMetrics;
import java.util.List;

public final class MarketRegimeEvaluator {

    private static final int BREAKOUT_PERIOD = 20;
    private static final double RANGE_MA_DISTANCE_MAX = 2.0;
    private static final double RANGE_SMA20_SLOPE_MAX = 1.0;
    private static final double RANGE_RSI_MIN = 40.0;
    private static final double RANGE_RSI_MAX = 60.0;
    private static final double COMPRESSION_RATIO_MAX = 0.75;

    private MarketRegimeEvaluator() {}


    public static MarketRegime evaluate(StockData stockData, SignalMetrics metrics)
    {
        validateInputs(stockData, metrics);

        if (isFalseBullishBreakout(stockData, metrics))
        {
            return MarketRegime.FALSE_BREAKOUT_UP;
        }

        if (isFalseBearishBreakdown(stockData, metrics))
        {
            return MarketRegime.FALSE_BREAKOUT_DOWN;
        }

        if (isCompression(metrics))
        {
            return MarketRegime.COMPRESSION;
        }

        if (isRangeBound(metrics))
        {
            return MarketRegime.RANGE_BOUND;
        }
        return MarketRegime.NORMAL;
    }

    private static boolean isFalseBullishBreakout(StockData stockData, SignalMetrics metrics)
    {
        double breakoutLevel = calculatePreviousDayHigh20(stockData, metrics.dataIndex());
        return metrics.previousClose() > breakoutLevel && metrics.close() < breakoutLevel;
    }

    private static boolean isFalseBearishBreakdown(StockData stockData, SignalMetrics metrics)
    {
        double breakdownLevel = calculatePreviousDayLow20(stockData, metrics.dataIndex());
        return metrics.previousClose() < breakdownLevel && metrics.close() > breakdownLevel;
    }


    private static boolean isCompression(SignalMetrics metrics)
    {
        if (metrics.averageBandwidth20() <= 0.0)
        {
            return false;
        }
        return metrics.bandwidthRelativeToAverage() <= COMPRESSION_RATIO_MAX;
    }


    private static boolean isRangeBound(SignalMetrics metrics)
    {
        boolean movingAveragesClose = metrics.movingAverageDistancePercent() <= RANGE_MA_DISTANCE_MAX;
        boolean sma20Flat = Math.abs(metrics.sma20SlopePercent()) <= RANGE_SMA20_SLOPE_MAX;

        boolean neutralRsi = metrics.rsi14() >= RANGE_RSI_MIN && metrics.rsi14() <= RANGE_RSI_MAX;
        boolean bandwidthBelowAverage = metrics.averageBandwidth20() > 0.0 && metrics.bandwidthRelativeToAverage() <= 1.0;

        boolean insidePreviousRange = metrics.close() <= metrics.previousHighestHigh20()
                        && metrics.close() >= metrics.previousLowestLow20();

        boolean insideBollingerBands = metrics.close() <= metrics.bollingerUpper20()
                        && metrics.close() >= metrics.bollingerLower20();

        return movingAveragesClose
                && sma20Flat
                && neutralRsi
                && bandwidthBelowAverage
                && insidePreviousRange
                && insideBollingerBands;
    }


    private static double calculatePreviousDayHigh20(StockData stockData, int endIndex)
    {
        return IndicatorCalculator.calculateHighestHigh(stockData, BREAKOUT_PERIOD, endIndex - 2);
    }


    private static double calculatePreviousDayLow20(StockData stockData, int endIndex)
    {
        return IndicatorCalculator.calculateLowestLow(stockData, BREAKOUT_PERIOD, endIndex - 2);
    }

    /**
     * Returns a short explanation for console output and
     * the future GUI.
     */
    public static String getExplanation(MarketRegime regime)
    {
        if (regime == null)
        {
            return "Market regime is unavailable";
        }

        return switch (regime) {
            case NORMAL ->
                    "No strong range, compression, or false-breakout condition";

            case RANGE_BOUND ->
                    "Price and moving averages indicate a sideways market";

            case COMPRESSION ->
                    "Bollinger bandwidth is unusually low; a larger move may follow";

            case FALSE_BREAKOUT_UP ->
                    "A bullish breakout failed and price returned below resistance";

            case FALSE_BREAKOUT_DOWN ->
                    "A bearish breakdown failed and price returned above support";
        };
    }

    private static void validateInputs(
            StockData stockData,
            SignalMetrics metrics
    ) {
        if (stockData == null) {
            throw new IllegalArgumentException(
                    "Stock data cannot be null"
            );
        }

        if (metrics == null) {
            throw new IllegalArgumentException(
                    "Signal metrics cannot be null"
            );
        }

        if (!stockData.getSymbol()
                .equalsIgnoreCase(metrics.ticker())) {
            throw new IllegalArgumentException(
                    "Stock data and metrics ticker do not match"
            );
        }

        List<DailyPrice> prices =
                stockData.getDailyPrices();

        if (metrics.dataIndex() < BREAKOUT_PERIOD + 1) {
            throw new IllegalArgumentException(
                    "Not enough history to evaluate market regime"
            );
        }

        if (metrics.dataIndex() >= prices.size()) {
            throw new IllegalArgumentException(
                    "Metric index is outside the stock-data range"
            );
        }

        DailyPrice selectedDay =
                prices.get(metrics.dataIndex());

        if (!selectedDay.getDate().equals(metrics.date())) {
            throw new IllegalArgumentException(
                    "Metric date does not match stock data"
            );
        }
    }
}