package GUI;

import Dataloader.StockData;
import Dataloader.StockDataLoader;
import Downloader.StockTickerList;
import SignalEvaluator.StockSignalEvaluator;
import SignalEvaluator.StockSignalResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import SignalEvaluator.MarketRegime;
import Systems.SystemSignal;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Connects the GUI to:
 *
 * 1. Ticker.txt
 * 2. Saved JSON files
 * 3. StockSignalEvaluator
 *
 * It does not display JavaFX controls.
 */
public final class StockSignalService
{
    private final Path stockDataFolder;

    /*
     * Complete backend results are stored here.
     *
     * Future S1–S4 pages will use these instead of recalculating
     * everything from the WatchlistRow.
     */
    private final Map<String, StockSignalResult> latestResults =
            new LinkedHashMap<>();

    private final Map<String, StockData> latestStockData =
            new LinkedHashMap<>();


    public StockSignalService(
            Path stockDataFolder)
    {
        if (stockDataFolder == null)
        {
            throw new IllegalArgumentException(
                    "Stock data folder cannot be null"
            );
        }

        this.stockDataFolder =
                stockDataFolder;
    }


    /**
     * Input:
     * - Ticker.txt
     * - JSON files inside stock_data
     *
     * Process:
     * - Load active tickers.
     * - Find their saved StockData.
     * - Evaluate their latest trading day.
     * - Convert results into WatchlistRow objects.
     *
     * Output:
     * - LoadResult containing rows and processing counts.
     */
    public LoadResult loadLatest()
            throws IOException
    {
        StockTickerList tickerList =
                new StockTickerList();

        List<String> trackedTickers =
                tickerList.getQuote();

        Map<String, StockData> allSavedStocks =
                StockDataLoader.loadAllStock(
                        stockDataFolder
                );

        /*
         * Normalize every map key to uppercase so ticker matching
         * remains consistent.
         */
        Map<String, StockData> normalizedSavedStocks =
                new HashMap<>();

        for (Map.Entry<String, StockData> entry
                : allSavedStocks.entrySet())
        {
            normalizedSavedStocks.put(
                    cleanTicker(entry.getKey()),
                    entry.getValue()
            );
        }

        /*
         * Only stocks still listed in Ticker.txt are considered active.
         */
        Map<String, StockData> activeStockMap =
                new LinkedHashMap<>();

        int missingDataCount = 0;

        for (String ticker : trackedTickers)
        {
            String cleanedTicker =
                    cleanTicker(ticker);

            StockData stockData =
                    normalizedSavedStocks.get(
                            cleanedTicker
                    );

            if (stockData == null)
            {
                missingDataCount++;
            }
            else
            {
                activeStockMap.put(
                        cleanedTicker,
                        stockData
                );
            }
        }

        latestStockData.clear();
        latestStockData.putAll(
                activeStockMap
        );

        List<StockSignalResult> evaluatedResults =
                StockSignalEvaluator.evaluateAllLatest(
                        activeStockMap
                );

        latestResults.clear();

        for (StockSignalResult result
                : evaluatedResults)
        {
            latestResults.put(
                    cleanTicker(result.ticker()),
                    result
            );
        }

        /*
         * Build rows in the same order as Ticker.txt. Every tracked ticker is
         * included, even when it has no JSON file or cannot produce complete
         * metrics. This keeps invalid/unavailable tickers selectable so the
         * user can remove them from the GUI.
         */
        List<WatchlistRow> rows =
                new ArrayList<>();

        for (String ticker : trackedTickers)
        {
            String cleanedTicker =
                    cleanTicker(ticker);

            StockSignalResult evaluatedResult =
                    latestResults.get(cleanedTicker);

            if (evaluatedResult != null)
            {
                rows.add(
                        WatchlistRow.from(
                                evaluatedResult
                        )
                );
            }
            else if (activeStockMap.containsKey(cleanedTicker))
            {
                rows.add(
                        WatchlistRow.unavailable(
                                cleanedTicker,
                                "INCOMPLETE DATA"
                        )
                );
            }
            else
            {
                rows.add(
                        WatchlistRow.unavailable(
                                cleanedTicker,
                                "NO DATA - CHECK TICKER"
                        )
                );
            }
        }

        int skippedEvaluationCount =
                activeStockMap.size()
                        - evaluatedResults.size();

        return new LoadResult(
                rows,
                trackedTickers.size(),
                activeStockMap.size(),
                evaluatedResults.size(),
                missingDataCount,
                skippedEvaluationCount
        );
    }


    /**
     * Adds a ticker to Ticker.txt.
     *
     * @return true if added, false if it already existed.
     */
    public boolean addTicker(
            String ticker)
            throws IOException
    {
        String cleanedTicker =
                cleanTicker(ticker);

        StockTickerList tickerList =
                new StockTickerList();

        return tickerList.addTicker(
                cleanedTicker
        );
    }


    /**
     * Removes a ticker from Ticker.txt.
     *
     * Its saved JSON price history is deliberately kept.
     *
     * @return true if removed, false if it was not present.
     */
    public boolean removeTicker(
            String ticker)
            throws IOException
    {
        String cleanedTicker =
                cleanTicker(ticker);

        StockTickerList tickerList =
                new StockTickerList();

        boolean removed =
                tickerList.removeTicker(
                        cleanedTicker
                );

        if (removed)
        {
            latestResults.remove(
                    cleanedTicker
            );
        }

        return removed;
    }


    /**
     * Checks whether stock_data/TICKER.json exists.
     */
    public boolean hasSavedData(
            String ticker)
    {
        String cleanedTicker =
                cleanTicker(ticker);

        Path filePath =
                stockDataFolder.resolve(
                        cleanedTicker + ".json"
                );

        return Files.isRegularFile(
                filePath
        );
    }


    /**
     * Finds one complete backend result.
     *
     * Future system-detail pages will use this.
     */
    public Optional<StockSignalResult> findResult(
            String ticker)
    {
        if (ticker == null
                || ticker.isBlank())
        {
            return Optional.empty();
        }

        return Optional.ofNullable(
                latestResults.get(
                        cleanTicker(ticker)
                )
        );
    }


    public List<StockSignalResult> getLatestResults()
    {
        return List.copyOf(
                latestResults.values()
        );
    }


    public int getResultCount()
    {
        return latestResults.size();
    }


    public Path getStockDataFolder()
    {
        return stockDataFolder;
    }


    private static String cleanTicker(
            String ticker)
    {
        if (ticker == null)
        {
            throw new IllegalArgumentException(
                    "Ticker cannot be null"
            );
        }

        String cleanedTicker =
                ticker.trim()
                        .toUpperCase(Locale.ROOT);

        if (!cleanedTicker.matches(
                "[A-Z0-9.-]{1,10}"
        ))
        {
            throw new IllegalArgumentException(
                    "Ticker contains invalid characters"
            );
        }

        return cleanedTicker;
    }


    /**
     * Summary of one complete watchlist-loading operation.
     */
    public record LoadResult(
            List<WatchlistRow> rows,
            int trackedTickerCount,
            int availableDataCount,
            int evaluatedStockCount,
            int missingDataCount,
            int skippedEvaluationCount)
    {
        public LoadResult
        {
            if (rows == null)
            {
                throw new IllegalArgumentException(
                        "Rows cannot be null"
                );
            }

            if (trackedTickerCount < 0
                    || availableDataCount < 0
                    || evaluatedStockCount < 0
                    || missingDataCount < 0
                    || skippedEvaluationCount < 0)
            {
                throw new IllegalArgumentException(
                        "Stock counts cannot be negative"
                );
            }

            rows = List.copyOf(rows);
        }
    }

    /**
     * Re-evaluates every active stock over the most recent
     * number of trading days.
     *
     * Input:
     * tradingDays = 7
     *
     * Output:
     * Directional trigger events from S1–S3 and regime changes from S4.
     */
    public List<SignalHistoryRow> buildHistory(
            int tradingDays
    ) {
        if (tradingDays <= 0) {
            throw new IllegalArgumentException(
                    "History window must be positive"
            );
        }

        List<SignalHistoryRow> historyRows =
                new ArrayList<>();

        for (StockData stockData
                : latestStockData.values()) {
            buildStockHistory(
                    stockData,
                    tradingDays,
                    historyRows
            );
        }

        historyRows.sort(
                Comparator
                        .comparing(
                                SignalHistoryRow::date
                        )
                        .reversed()
                        .thenComparing(
                                SignalHistoryRow::ticker,
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparingInt(
                                SignalHistoryRow::systemNumber
                        )
        );

        return List.copyOf(
                historyRows
        );
    }


    private void buildStockHistory(
            StockData stockData,
            int tradingDays,
            List<SignalHistoryRow> historyRows
    ) {
        int lastIndex =
                stockData.getDailyPrices()
                        .size()
                        - 1;

        int firstHistoryIndex =
                Math.max(
                        0,
                        lastIndex - tradingDays + 1
                );

        /*
         * Evaluate one additional preceding day so the first
         * history day can detect an S4 regime transition.
         */
        int evaluationStartIndex =
                Math.max(
                        0,
                        firstHistoryIndex - 1
                );

        MarketRegime previousRegime =
                null;

        for (int index = evaluationStartIndex;
             index <= lastIndex;
             index++) {
            try {
                StockSignalResult result =
                        StockSignalEvaluator.evaluate(
                                stockData,
                                index
                        );

                if (index >= firstHistoryIndex) {
                    addDirectionalHistoryEvent(
                            historyRows,
                            result,
                            result.system1()
                    );

                    addDirectionalHistoryEvent(
                            historyRows,
                            result,
                            result.system2()
                    );

                    addDirectionalHistoryEvent(
                            historyRows,
                            result,
                            result.system3()
                    );

                    if (previousRegime != null
                            && previousRegime
                            != result.marketRegime()) {
                        historyRows.add(
                                SignalHistoryRow
                                        .fromRegimeChange(
                                                result,
                                                previousRegime
                                        )
                        );
                    }
                }

                previousRegime =
                        result.marketRegime();
            }
            catch (IllegalArgumentException exception) {
                /*
                 * A very early index may not contain enough historical
                 * data for SMA50 and other indicators.
                 *
                 * Skip only that date.
                 */
                previousRegime = null;
            }
        }
    }


    private void addDirectionalHistoryEvent(
            List<SignalHistoryRow> historyRows,
            StockSignalResult result,
            SystemSignal signal
    ) {
        /*
         * Use rawDirection rather than finalDirection.
         *
         * A muted signal has:
         * rawDirection = BUY or SHORT
         * finalDirection = NEUTRAL
         */
        if (!signal.rawDirection()
                .isDirectional()) {
            return;
        }

        historyRows.add(
                SignalHistoryRow.fromSystem(
                        result,
                        signal
                )
        );
    }
}