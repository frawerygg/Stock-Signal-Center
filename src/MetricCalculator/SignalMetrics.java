package MetricCalculator;

import java.time.LocalDate;
import java.util.Locale;


public record SignalMetrics(
        String ticker,
        LocalDate date,
        int dataIndex,

        double open,
        double high,
        double low,
        double close,
        long volume,

        double previousClose,
        double previousHigh,
        double previousLow,

        double sma20,
        double sma50,
        double sma20FiveDaysAgo,
        double sma50FiveDaysAgo,

        double previousAverageVolume20,
        double relativeVolume20,

        double atr14,
        double atrPercent14,
        double rsi14,

        double previousHighestHigh20,
        double previousLowestLow20,

        double previousHighestHigh7,
        double previousLowestLow7,

        double bollingerMiddle20,
        double bollingerUpper20,
        double bollingerLower20,

        double bollingerBandwidth20,
        double previousBandwidth20,
        double averageBandwidth20,

        double previousSma20,
        double previousSma50,

        double previousRsi14,
        double previousPriceChangePercent,

        double pullbackPeakClose21,
        double pullbackPeakAtr14,
        double pullbackTroughClose21,
        double pullbackTroughLow21
)

{

    public SignalMetrics
    {
        if (ticker == null || ticker.isBlank())
        {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }

        if (date == null)
        {
            throw new IllegalArgumentException("Metric date cannot be null");
        }

        if (dataIndex < 0)
        {throw new IllegalArgumentException("Data index cannot be negative");
        }

        if (open <= 0.0 || high <= 0.0 || low <= 0.0 || close <= 0.0)
        {
            throw new IllegalArgumentException("OHLC prices must be positive");
        }

        if (high < low)
        {throw new IllegalArgumentException("Daily high cannot be below daily low");
        }

        if (volume < 0L)
        {
            throw new IllegalArgumentException("Volume cannot be negative");
        }

        if (previousClose <= 0.0 || previousHigh <= 0.0 || previousLow <= 0.0)
        {
            throw new IllegalArgumentException("Previous-day prices must be positive");
        }

        if (previousHigh < previousLow)
        {
            throw new IllegalArgumentException("Previous high cannot be below previous low");
        }

        if (previousAverageVolume20 < 0.0)
        {
            throw new IllegalArgumentException("Average volume cannot be negative");
        }

        if (relativeVolume20 < 0.0)
        {
            throw new IllegalArgumentException("Relative volume cannot be negative");
        }

        if (atr14 < 0.0 || atrPercent14 < 0.0)
        {
            throw new IllegalArgumentException("ATR values cannot be negative");
        }

        if (rsi14 < 0.0 || rsi14 > 100.0)
        {
            throw new IllegalArgumentException("RSI must be between 0 and 100");
        }

        if (previousHighestHigh20 < previousLowestLow20)
        {
            throw new IllegalArgumentException("Previous 20-day high cannot be below its low");
        }

        if (previousHighestHigh7 < previousLowestLow7)
        {
            throw new IllegalArgumentException("Previous seven-day high cannot be below its low");
        }

        if (bollingerUpper20 < bollingerMiddle20 || bollingerMiddle20 < bollingerLower20)
        {
            throw new IllegalArgumentException("Bollinger Bands are in an invalid order");
        }

        if (bollingerBandwidth20 < 0.0 || previousBandwidth20 < 0.0 || averageBandwidth20 < 0.0)
        {
            throw new IllegalArgumentException("Bollinger bandwidth cannot be negative");
        }

        if (previousRsi14 < 0.0 || previousRsi14 > 100.0)
        {
            throw new IllegalArgumentException(
                    "Previous RSI must be between 0 and 100"
            );
        }

        if (!Double.isFinite(previousPriceChangePercent))
        {
            throw new IllegalArgumentException(
                    "Previous price change must be finite"
            );
        }

        if (pullbackPeakClose21 <= 0.0
                || pullbackTroughClose21 <= 0.0
                || pullbackTroughLow21 <= 0.0)
        {
            throw new IllegalArgumentException(
                    "Pullback prices must be positive"
            );
        }

        if (pullbackPeakAtr14 < 0.0)
        {
            throw new IllegalArgumentException(
                    "Peak ATR cannot be negative"
            );
        }

        if (pullbackTroughClose21 > pullbackPeakClose21)
        {
            throw new IllegalArgumentException(
                    "Pullback trough cannot be above its peak"
            );
        }

        if (pullbackTroughLow21 > pullbackTroughClose21)
        {
            throw new IllegalArgumentException(
                    "Trough low cannot be above trough close"
            );
        }
    }

    public double sma20SlopePercent()
    {
        if (sma20FiveDaysAgo == 0.0)
        {
            return 0.0;
        }
        return (sma20 - sma20FiveDaysAgo) / sma20FiveDaysAgo * 100.0;
    }

    public double movingAverageDistancePercent()
    {
        return Math.abs(sma20 - sma50) / close * 100.0;
    }

    public double bandwidthRelativeToAverage()
    {
        if (averageBandwidth20 == 0.0)
        {
            return 0.0;
        }
        return bollingerBandwidth20 / averageBandwidth20;
    }

    public double priceChangePercent()
    {
        if (previousClose == 0.0)
        {
            return 0.0;
        }
        return (close - previousClose) / previousClose * 100.0;
    }

    public double pullbackDrawdownPercent21()
    {
        if (pullbackPeakClose21 == 0.0)
        {
            return 0.0;
        }

        return (pullbackPeakClose21 - pullbackTroughClose21) / pullbackPeakClose21 * 100.0;
    }

    public double pullbackDrawdownAtrMultiple21()
    {
        if (pullbackPeakAtr14 == 0.0)
        {
            return 0.0;
        }

        return (pullbackPeakClose21 - pullbackTroughClose21) / pullbackPeakAtr14;
    }

    public double recoveryFromTroughAtrMultiple21()
    {
        if (atr14 == 0.0)
        {
            return 0.0;
        }

        return (close - pullbackTroughClose21) / atr14;
    }

    public double priceAccelerationPercent()
    {
        return priceChangePercent() - previousPriceChangePercent;
    }


    @Override
    public String toString()
    {
        return "SignalMetrics{" +
                "ticker='" + ticker + '\'' +
                ", date=" + date +
                ", close=" + format(close) +
                ", change=" + format(priceChangePercent()) + "%" +
                ", sma20=" + format(sma20) +
                ", sma50=" + format(sma50) +
                ", rsi14=" + format(rsi14) +
                ", relativeVolume20=" + format(relativeVolume20) +
                ", atr14=" + format(atr14) +
                ", high20=" + format(previousHighestHigh20) +
                ", low20=" + format(previousLowestLow20) +
                ", bandwidth=" + format(bollingerBandwidth20) +
                '}';
    }

    private static String format(double value)
    {
        return String.format(Locale.US, "%.2f", value);
    }
}