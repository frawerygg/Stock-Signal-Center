package MetricCalculator;
import Dataloader.DailyPrice;
import Dataloader.StockData;
import java.util.List;

public class IndicatorCalculator
{

    private IndicatorCalculator()
    {}

    private static List<DailyPrice> getValidatedPrices(StockData stockData)
    {
        if (stockData == null)
        {
            throw new IllegalArgumentException("Stock data cannot be null");
        }

        List<DailyPrice> prices = stockData.getDailyPrices();

        if (prices == null || prices.isEmpty())
        {
            throw new IllegalArgumentException("Price data cannot be empty");
        }
        return prices;
    }

    private static void validatePeriod(int period)
    {
        if (period <= 0)
        {
            throw new IllegalArgumentException("Period must be greater than zero");
        }
    }

    private static void validateEndIndex(List<DailyPrice> prices, int endIndex)
    {
        if (endIndex < 0 || endIndex >= prices.size())
        {
            throw new IllegalArgumentException("End index is outside the price-data range: " + endIndex);
        }
    }

    private static int validateWindow(List<DailyPrice> prices, int period, int endIndex, String indicatorName)
    {
        validatePeriod(period);
        validateEndIndex(prices, endIndex);

        int startIndex = endIndex - period + 1;

        if (startIndex < 0)
        {
            throw new IllegalArgumentException("Not enough data to calculate " + indicatorName + " with period " + period + " at index " + endIndex);
        }

        return startIndex;
    }

    public static double calculateSMA(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        int startIndex = validateWindow(prices, period, endIndex, "SMA");
        double closingPriceSum = 0.0;
        for (int i = startIndex; i <= endIndex; i++)
        {
            closingPriceSum += prices.get(i).getClose();
        }
        return closingPriceSum / period;
    }

    public static double calculateAverageVolume(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        int startIndex = validateWindow(prices, period, endIndex, "average volume");
        double volumeSum = 0.0;
        for (int i = startIndex; i <= endIndex; i++)
        {
            volumeSum += prices.get(i).getVolume();
        }
        return volumeSum / period;
    }

    public static double calculateRelativeVolume(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        validatePeriod(period);
        validateEndIndex(prices, endIndex);
        if (endIndex < period)
        {
            throw new IllegalArgumentException(
                    "Not enough data to calculate relative volume "
                            + "with period "
                            + period
                            + " at index "
                            + endIndex
            );
        }
        double currentVolume = prices.get(endIndex).getVolume();
        double previousAverageVolume = calculateAverageVolume(stockData, period, endIndex - 1);
        if (previousAverageVolume == 0.0)
        {
            throw new IllegalArgumentException("Previous average volume is zero");
        }

        return currentVolume / previousAverageVolume;
    }

    public static double calculateTrueRange(StockData stockData, int index)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        if (index <= 0 || index >= prices.size())
        {
            throw new IllegalArgumentException("True-range index must be between 1 and " + (prices.size() - 1));
        }
        DailyPrice today = prices.get(index);
        DailyPrice yesterday = prices.get(index - 1);
        double highLowRange = today.getHigh() - today.getLow();
        double highPreviousCloseRange = Math.abs(today.getHigh() - yesterday.getClose());
        double lowPreviousCloseRange = Math.abs(today.getLow() - yesterday.getClose());

        return Math.max(highLowRange, Math.max(highPreviousCloseRange, lowPreviousCloseRange));
    }


    public static double calculateATR(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        int startIndex = validateWindow(prices, period, endIndex, "ATR");

        if (startIndex == 0)
        {
            throw new IllegalArgumentException("Not enough previous-price data to calculate " + "ATR with period " + period + " at index " + endIndex);
        }

        double trueRangeSum = 0.0;

        for (int i = startIndex; i <= endIndex; i++)
        {
            trueRangeSum += calculateTrueRange(stockData, i);
        }

        return trueRangeSum / period;
    }

    public static double calculateHighestHigh(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        int startIndex = validateWindow(prices, period, endIndex, "highest high");

        double highestHigh = prices.get(startIndex).getHigh();
        for (int i = startIndex + 1; i <= endIndex; i++)
        {
            double currentHigh = prices.get(i).getHigh();
            if (currentHigh > highestHigh)
            {
                highestHigh = currentHigh;
            }
        }
        return highestHigh;
    }

    public static double calculateLowestLow(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);
        int startIndex = validateWindow(prices, period, endIndex, "lowest low");
        double lowestLow = prices.get(startIndex).getLow();

        for (int i = startIndex + 1; i <= endIndex; i++)
        {
            double currentLow = prices.get(i).getLow();
            if (currentLow < lowestLow)
            {
                lowestLow = currentLow;
            }
        }
        return lowestLow;
    }


    public static double calculateRSI(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);

        int startIndex = validateWindow(prices, period, endIndex, "RSI");

        if (startIndex == 0)
        {
            throw new IllegalArgumentException("Not enough previous-price data to calculate " + "RSI with period " + period + " at index " + endIndex);
        }

        double gainSum = 0.0;
        double lossSum = 0.0;

        for (int i = startIndex; i <= endIndex; i++)
        {
            double todayClose = prices.get(i).getClose();
            double yesterdayClose = prices.get(i - 1).getClose();
            double change = todayClose - yesterdayClose;

            if (change > 0.0)
            {
                gainSum += change;
            }

            else if (change < 0.0)
            {
                lossSum += Math.abs(change);
            }
        }

        double averageGain = gainSum / period;
        double averageLoss = lossSum / period;

        if (averageLoss == 0.0)
        {
            return 100.0;
        }

        if (averageGain == 0.0)
        {
            return 0.0;
        }

        double relativeStrength = averageGain / averageLoss;

        return 100.0 - (100.0 / (1.0 + relativeStrength));
    }

    public static double calculateStandardDeviation(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);

        int startIndex = validateWindow(prices, period, endIndex, "standard deviation");
        double sma = calculateSMA(stockData, period, endIndex);
        double squaredDifferenceSum = 0.0;

        for (int i = startIndex; i <= endIndex; i++)
        {
            double close = prices.get(i).getClose();
            double difference = close - sma;
            squaredDifferenceSum += difference * difference;
        }

        return Math.sqrt(squaredDifferenceSum / period);
    }

    public static double calculateBollingerMiddle(StockData stockData, int period, int endIndex)
    {
        return calculateSMA(stockData, period, endIndex);
    }


    public static double calculateBollingerUpper(StockData stockData, int period, double standardDeviationMultiplier, int endIndex)
    {
        validateStandardDeviationMultiplier(standardDeviationMultiplier);
        double middleBand = calculateBollingerMiddle(stockData, period, endIndex);
        double standardDeviation = calculateStandardDeviation(stockData, period, endIndex);
        return middleBand + standardDeviationMultiplier * standardDeviation;
    }


    public static double calculateBollingerLower(StockData stockData, int period, double standardDeviationMultiplier, int endIndex)
    {
        validateStandardDeviationMultiplier(standardDeviationMultiplier);
        double middleBand = calculateBollingerMiddle(stockData, period, endIndex);
        double standardDeviation = calculateStandardDeviation(stockData, period, endIndex);
        return middleBand - standardDeviationMultiplier * standardDeviation;
    }


    public static double calculateBollingerBandwidth(StockData stockData, int period, double standardDeviationMultiplier, int endIndex)
    {
        double middleBand = calculateBollingerMiddle(stockData, period, endIndex);
        double upperBand = calculateBollingerUpper(stockData, period, standardDeviationMultiplier, endIndex);
        double lowerBand = calculateBollingerLower(stockData, period, standardDeviationMultiplier, endIndex);

        if (middleBand == 0.0)
        {
            throw new IllegalArgumentException(
                    "Middle Bollinger Band cannot be zero"
            );
        }
        return (upperBand - lowerBand) / middleBand;
    }


    private static void validateStandardDeviationMultiplier(
            double standardDeviationMultiplier)
    {
        if (standardDeviationMultiplier < 0.0)
        {
            throw new IllegalArgumentException(
                    "Standard-deviation multiplier "
                            + "cannot be negative"
            );
        }

    }

    public static int calculateHighestCloseIndex(StockData stockData, int period, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);

        int startIndex = validateWindow(prices, period, endIndex, "highest close index");

        int highestIndex = startIndex;
        double highestClose = prices.get(startIndex).getClose();

        for (int index = startIndex + 1; index <= endIndex; index++)
        {
            double currentClose = prices.get(index).getClose();
            if (currentClose > highestClose)
            {
                highestClose = currentClose;
                highestIndex = index;
            }
        }
        return highestIndex;
    }

    public static int calculateLowestCloseIndex(StockData stockData, int startIndex, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);

        validateEndIndex(prices, startIndex);
        validateEndIndex(prices, endIndex);

        if (startIndex > endIndex)
        {
            throw new IllegalArgumentException("Start index cannot be after end index");
        }

        int lowestIndex = startIndex;
        double lowestClose = prices.get(startIndex).getClose();

        for (int index = startIndex + 1; index <= endIndex; index++)
        {
            double currentClose = prices.get(index).getClose();

            if (currentClose < lowestClose)
            {
                lowestClose = currentClose;
                lowestIndex = index;
            }
        }
        return lowestIndex;
    }

    public static double calculatePriceChangePercent(StockData stockData, int endIndex)
    {
        List<DailyPrice> prices = getValidatedPrices(stockData);

        validateEndIndex(prices, endIndex);

        if (endIndex < 1)
        {
            throw new IllegalArgumentException("At least one previous day is required");
        }

        double currentClose = prices.get(endIndex).getClose();
        double previousClose = prices.get(endIndex - 1).getClose();
        return (currentClose - previousClose) / previousClose * 100.0;
    }

    public static double calculatePriceAccelerationPercent(StockData stockData, int endIndex)
    {
        if (endIndex < 2)
        {
            throw new IllegalArgumentException("At least two previous days are required");
        }

        double currentChange = calculatePriceChangePercent(stockData, endIndex);
        double previousChange = calculatePriceChangePercent(stockData, endIndex - 1);
        return currentChange - previousChange;
    }
}
