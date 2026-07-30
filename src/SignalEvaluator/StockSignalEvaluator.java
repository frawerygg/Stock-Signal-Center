package SignalEvaluator;

import Dataloader.StockData;
import MetricCalculator.SignalMetrics;
import MetricCalculator.StockMetricCalculator;
import Systems.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public final class StockSignalEvaluator
{
    private StockSignalEvaluator() {}

    public static StockSignalResult evaluateLatest(StockData stockData)
    {
        validateStockData(stockData);
        int lastIndex = stockData.getDailyPrices().size() - 1;
        return evaluate(stockData, lastIndex);
    }


    public static StockSignalResult evaluate(StockData stockData, int endIndex)
    {
        validateStockData(stockData);

        SignalMetrics metrics = StockMetricCalculator.calculate(stockData, endIndex);

        /*
         * Step 2:
         * Run the three directional systems.
         */
        SystemSignal system1 =
                TrendContinuationEvaluator.evaluate(
                        metrics
                );

        SystemSignal system2 =
                BreakoutEvaluator.evaluate(
                        metrics
                );

        SystemSignal system3 =
                PullbackEvaluator.evaluate(
                        metrics
                );

        /*
         * Step 3:
         * Determine the current market environment.
         */
        MarketRegime marketRegime =
                MarketRegimeEvaluator.evaluate(
                        stockData,
                        metrics
                );

        /*
         * Step 4:
         * Apply System 4 filtering and combine Systems 1–3.
         */
        SignalResolution resolution =
                SignalResolver.resolve(
                        system1,
                        system2,
                        system3,
                        marketRegime
                );

        /*
         * Step 5:
         * Evaluate the options-income candidate.
         */
        OptionStrategyEvaluation optionEvaluation =
                OptionStrategyEvaluator.evaluate(
                        metrics,
                        resolution
                );

        /*
         * Step 6:
         * Return one complete GUI-ready result.
         */
        return new StockSignalResult(
                metrics,
                resolution,
                optionEvaluation
        );
    }

    /**
     * Evaluates the latest date for every loaded stock.
     *
     * Stocks without enough historical data are skipped.
     */
    public static List<StockSignalResult> evaluateAllLatest(
            Map<String, StockData> stockMap
    ) {
        if (stockMap == null) {
            throw new IllegalArgumentException(
                    "Stock map cannot be null"
            );
        }

        List<StockSignalResult> results =
                new ArrayList<>();

        for (StockData stockData : stockMap.values()) {
            try {
                StockSignalResult result =
                        evaluateLatest(stockData);

                results.add(result);
            }
            catch (IllegalArgumentException exception) {
                /*
                 * One incomplete stock should not prevent the GUI
                 * from displaying every other valid stock.
                 */
            }
        }

        results.sort(
                Comparator.comparing(
                        StockSignalResult::ticker,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return results;
    }

    private static void validateStockData(
            StockData stockData
    ) {
        if (stockData == null) {
            throw new IllegalArgumentException(
                    "Stock data cannot be null"
            );
        }

        if (stockData.getDailyPrices() == null
                || stockData.getDailyPrices().isEmpty()) {
            throw new IllegalArgumentException(
                    "Stock price data cannot be empty"
            );
        }
    }
}