package MetricCalculator;
import Dataloader.DailyPrice;
import Dataloader.StockData;
import java.util.List;

public final class StockMetricCalculator
{
    private static final int SMA_SHORT_PERIOD = 20;
    private static final int SMA_LONG_PERIOD = 50;
    private static final int SMA_SLOPE_LOOKBACK = 5;

    private static final int VOLUME_PERIOD = 20;

    private static final int ATR_PERIOD = 14;
    private static final int RSI_PERIOD = 14;

    private static final int BREAKOUT_PERIOD = 20;
    private static final int RECENT_PERIOD = 7;
    private static final int PULLBACK_PERIOD = 21;

    private static final int BOLLINGER_PERIOD = 20;
    private static final double BOLLINGER_MULTIPLIER = 2.0;
    private static final int BANDWIDTH_AVERAGE_PERIOD = 20;

    private static final int MINIMUM_REQUIRED_INDEX = SMA_LONG_PERIOD - 1 + SMA_SLOPE_LOOKBACK;

    private StockMetricCalculator() {}

    public static SignalMetrics calculate(StockData stockData, int endIndex)
    {
        validateStockData(stockData);
        List<DailyPrice> prices = stockData.getDailyPrices();
        validateEndIndex(prices, endIndex);

        if (endIndex < MINIMUM_REQUIRED_INDEX)
        {
            throw new IllegalArgumentException("Not enough historical data for " + stockData.getSymbol() + " at index " + endIndex + ". Minimum valid index is " + MINIMUM_REQUIRED_INDEX);
        }

        DailyPrice currentDay = prices.get(endIndex);
        DailyPrice previousDay = prices.get(endIndex - 1);

        // ------------------------------------------------------------
        // Moving averages
        // ------------------------------------------------------------

        double sma20 = IndicatorCalculator.calculateSMA(stockData, SMA_SHORT_PERIOD, endIndex);
        double sma50 = IndicatorCalculator.calculateSMA(stockData, SMA_LONG_PERIOD, endIndex);
        double sma20FiveDaysAgo = IndicatorCalculator.calculateSMA(stockData, SMA_SHORT_PERIOD, endIndex - SMA_SLOPE_LOOKBACK);
        double sma50FiveDaysAgo = IndicatorCalculator.calculateSMA(stockData, SMA_LONG_PERIOD, endIndex - SMA_SLOPE_LOOKBACK);


        double previousSma20 = IndicatorCalculator.calculateSMA(stockData, SMA_SHORT_PERIOD, endIndex - 1);

        double previousSma50 = IndicatorCalculator.calculateSMA(stockData, SMA_LONG_PERIOD, endIndex - 1);


        // ------------------------------------------------------------
        // Volume
        // ------------------------------------------------------------

        double previousAverageVolume20 = IndicatorCalculator.calculateAverageVolume(stockData, VOLUME_PERIOD, endIndex - 1);

        if (previousAverageVolume20 <= 0.0)
        {
            throw new IllegalArgumentException("Previous average volume must be positive");
        }

        double relativeVolume20 = currentDay.getVolume() / previousAverageVolume20;

        // ------------------------------------------------------------
        // ATR and RSI
        // ------------------------------------------------------------

        double atr14 = IndicatorCalculator.calculateATR(stockData, ATR_PERIOD, endIndex);
        double atrPercent14 = calculateAtrPercent(atr14, currentDay.getClose());
        double rsi14 = IndicatorCalculator.calculateRSI(stockData, RSI_PERIOD, endIndex);

        // ------------------------------------------------------------
        // Previous price ranges
        // ------------------------------------------------------------

        double previousHighestHigh20 = IndicatorCalculator.calculateHighestHigh(stockData, BREAKOUT_PERIOD, endIndex - 1);
        double previousLowestLow20 = IndicatorCalculator.calculateLowestLow(stockData, BREAKOUT_PERIOD, endIndex - 1);
        double previousHighestHigh7 = IndicatorCalculator.calculateHighestHigh(stockData, RECENT_PERIOD, endIndex - 1);
        double previousLowestLow7 = IndicatorCalculator.calculateLowestLow(stockData, RECENT_PERIOD, endIndex - 1);

        // ------------------------------------------------------------
        // Bollinger Bands
        // ------------------------------------------------------------

        double bollingerMiddle20 = IndicatorCalculator.calculateBollingerMiddle(stockData, BOLLINGER_PERIOD, endIndex);
        double bollingerUpper20 = IndicatorCalculator.calculateBollingerUpper(stockData, BOLLINGER_PERIOD, BOLLINGER_MULTIPLIER, endIndex);
        double bollingerLower20 = IndicatorCalculator.calculateBollingerLower(stockData, BOLLINGER_PERIOD, BOLLINGER_MULTIPLIER, endIndex);
        double bollingerBandwidth20 = calculateBandwidth(stockData, endIndex);
        double previousBandwidth20 = calculateBandwidth(stockData, endIndex - 1);
        double averageBandwidth20 = calculatePreviousAverageBandwidth(stockData, endIndex, BANDWIDTH_AVERAGE_PERIOD);

        // ------------------------------------------------------------
        // PullBack Recovery
        // ------------------------------------------------------------
        double previousRsi14 = IndicatorCalculator.calculateRSI(stockData, RSI_PERIOD, endIndex - 1);
        double previousPriceChangePercent = IndicatorCalculator.calculatePriceChangePercent(stockData, endIndex - 1);

        int pullbackPeakIndex21 = IndicatorCalculator.calculateHighestCloseIndex(stockData, PULLBACK_PERIOD, endIndex - 1);
        int pullbackTroughIndex21 = IndicatorCalculator.calculateLowestCloseIndex(stockData, pullbackPeakIndex21, endIndex - 1);

        DailyPrice pullbackPeakDay = prices.get(pullbackPeakIndex21);
        DailyPrice pullbackTroughDay = prices.get(pullbackTroughIndex21);

        double pullbackPeakClose21 = pullbackPeakDay.getClose();
        double pullbackPeakAtr14 = IndicatorCalculator.calculateATR(stockData, ATR_PERIOD, pullbackPeakIndex21);
        double pullbackTroughClose21 = pullbackTroughDay.getClose();
        double pullbackTroughLow21 = pullbackTroughDay.getLow();




        return new SignalMetrics(
                stockData.getSymbol(),
                currentDay.getDate(),
                endIndex,

                currentDay.getOpen(),
                currentDay.getHigh(),
                currentDay.getLow(),
                currentDay.getClose(),
                currentDay.getVolume(),

                previousDay.getClose(),
                previousDay.getHigh(),
                previousDay.getLow(),

                sma20,
                sma50,
                sma20FiveDaysAgo,
                sma50FiveDaysAgo,

                previousAverageVolume20,
                relativeVolume20,

                atr14,
                atrPercent14,
                rsi14,

                previousHighestHigh20,
                previousLowestLow20,

                previousHighestHigh7,
                previousLowestLow7,

                bollingerMiddle20,
                bollingerUpper20,
                bollingerLower20,

                bollingerBandwidth20,
                previousBandwidth20,
                averageBandwidth20,

                previousSma20,
                previousSma50,

                previousRsi14,
                previousPriceChangePercent,

                pullbackPeakClose21,
                pullbackPeakAtr14,
                pullbackTroughClose21,
                pullbackTroughLow21
        );
    }

    private static double calculatePreviousAverageBandwidth(StockData stockData, int endIndex, int period)
    {
        int startIndex = endIndex - period;
        int finalIndex = endIndex - 1;
        double sum = 0.0;

        for (int index = startIndex; index <= finalIndex; index++)
        {
            sum += calculateBandwidth(stockData, index);
        }
        return sum / period;
    }

    private static double calculateBandwidth(StockData stockData, int endIndex)
    {
        return IndicatorCalculator.calculateBollingerBandwidth(stockData, BOLLINGER_PERIOD, BOLLINGER_MULTIPLIER, endIndex);
    }

    private static double calculateAtrPercent(double atr, double close)
    {
        if (close <= 0.0)
        {
            throw new IllegalArgumentException("Closing price must be positive");
        }
        return atr / close * 100.0;
    }

    private static void validateStockData(StockData stockData)
    {
        if (stockData == null) {throw new IllegalArgumentException("Stock data cannot be null");
        }

        if (stockData.getDailyPrices() == null || stockData.getDailyPrices().isEmpty())
        {
            throw new IllegalArgumentException("Stock price data cannot be empty");
        }
    }

    private static void validateEndIndex(List<DailyPrice> prices, int endIndex)
    {
        if (endIndex < 0 || endIndex >= prices.size())
        {
            throw new IllegalArgumentException("End index is outside the price-data range: " + endIndex);
        }
    }
}