package GUI;

import MetricCalculator.SignalMetrics;
import SignalEvaluator.MarketRegime;
import SignalEvaluator.MarketRegimeEvaluator;
import SignalEvaluator.SignalResolution;
import SignalEvaluator.StockSignalResult;
import Systems.SystemSignal;

import java.time.LocalDate;
import java.util.Locale;

/**
 * One display row for the System 4 page.
 *
 * System 4 does not return a SystemSignal. It returns a MarketRegime,
 * which is then used by SignalResolver to filter Systems 1–3.
 */
public record System4Row(
        String ticker,
        LocalDate date,

        MarketRegime regime,
        String overallSignal,

        double price,
        double previousClose,

        double movingAverageDistancePercent,
        double sma20SlopePercent,
        double rsi14,

        double bandwidth20,
        double averageBandwidth20,
        double bandwidthRatio,

        double previousHighestHigh20,
        double previousLowestLow20,

        double bollingerUpper20,
        double bollingerLower20,

        String system1Effect,
        String system2Effect,
        String system3Effect,

        int bullishCount,
        int bearishCount,

        String regimeExplanation,
        String resolutionExplanation
) {
    public System4Row {
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

        if (regime == null) {
            throw new IllegalArgumentException(
                    "Market regime cannot be null"
            );
        }

        if (overallSignal == null
                || overallSignal.isBlank()) {
            throw new IllegalArgumentException(
                    "Overall signal cannot be blank"
            );
        }

        if (regimeExplanation == null) {
            regimeExplanation = "";
        }

        if (resolutionExplanation == null) {
            resolutionExplanation = "";
        }
    }


    /**
     * Input:
     * One complete StockSignalResult.
     *
     * Output:
     * One GUI-ready System4Row.
     */
    public static System4Row from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        SignalMetrics metrics =
                result.metrics();

        SignalResolution resolution =
                result.resolution();

        MarketRegime regime =
                result.marketRegime();

        return new System4Row(
                result.ticker(),
                result.date(),

                regime,
                result.overallDisplayName(),

                metrics.close(),
                metrics.previousClose(),

                metrics.movingAverageDistancePercent(),
                metrics.sma20SlopePercent(),
                metrics.rsi14(),

                metrics.bollingerBandwidth20(),
                metrics.averageBandwidth20(),
                metrics.bandwidthRelativeToAverage(),

                metrics.previousHighestHigh20(),
                metrics.previousLowestLow20(),

                metrics.bollingerUpper20(),
                metrics.bollingerLower20(),

                convertSystemEffect(
                        result.system1()
                ),

                convertSystemEffect(
                        result.system2()
                ),

                convertSystemEffect(
                        result.system3()
                ),

                result.bullishCount(),
                result.bearishCount(),

                MarketRegimeEvaluator.getExplanation(
                        regime
                ),

                resolution.explanation()
        );
    }


    /**
     * Examples:
     *
     * BUY
     * SHORT
     * NEUTRAL
     * BUY -> MUTED
     * SHORT -> MUTED
     */
    private static String convertSystemEffect(
            SystemSignal signal
    ) {
        if (signal.muted()) {
            return signal.rawDirection().name()
                    + " -> MUTED";
        }

        return signal.finalDirection().name();
    }


    public String regimeDisplayName() {
        return regime.getDisplayName()
                .toUpperCase(Locale.ROOT);
    }


    public boolean isNormal() {
        return regime == MarketRegime.NORMAL;
    }


    public boolean isRangeBound() {
        return regime
                == MarketRegime.RANGE_BOUND;
    }


    public boolean isCompression() {
        return regime
                == MarketRegime.COMPRESSION;
    }


    public boolean isFalseBreakout() {
        return regime
                == MarketRegime.FALSE_BREAKOUT_UP
                || regime
                == MarketRegime.FALSE_BREAKOUT_DOWN;
    }


    public boolean hasFilteringEffect() {
        return system1Effect.contains("MUTED")
                || system2Effect.contains("MUTED")
                || system3Effect.contains("MUTED");
    }


    public boolean isWarningRegime() {
        return isCompression()
                || isFalseBreakout();
    }
}