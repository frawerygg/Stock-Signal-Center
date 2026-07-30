package GUI;

import MetricCalculator.SignalMetrics;
import SignalEvaluator.MarketRegime;
import SignalEvaluator.StockSignalResult;
import Systems.SignalDirection;
import Systems.SystemSignal;

import java.time.LocalDate;
import java.util.Locale;

/**
 * One display row for the Watchlist Manager.
 *
 * Besides evaluated stocks, this record can also represent a ticker that is
 * still present in Ticker.txt but has no usable market-data file. Keeping
 * those tickers visible lets the user select and remove them from the GUI.
 */
public record WatchlistRow(
        String ticker,
        LocalDate date,

        double price,
        double changePercent,
        double atr14,
        double relativeVolume,

        String system1Status,
        String system2Status,
        String system3Status,

        MarketRegime marketRegime,
        String dataStatus,
        boolean dataAvailable
) {

    public WatchlistRow {
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

        if (marketRegime == null) {
            throw new IllegalArgumentException(
                    "Market regime cannot be null"
            );
        }

        if (dataStatus == null || dataStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Data status cannot be blank"
            );
        }
    }

    /**
     * Converts one complete backend result into one GUI table row.
     */
    public static WatchlistRow from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        SignalMetrics metrics = result.metrics();

        return new WatchlistRow(
                result.ticker(),
                result.date(),

                result.price(),
                metrics.priceChangePercent(),
                metrics.atr14(),
                metrics.relativeVolume20(),

                convertSystemStatus(result.system1()),
                convertSystemStatus(result.system2()),
                convertSystemStatus(result.system3()),

                result.marketRegime(),
                "READY",
                true
        );
    }

    /**
     * Creates a visible placeholder for a tracked ticker that could not be
     * evaluated. Numeric fields use NaN so the table renders a red N/A value.
     */
    public static WatchlistRow unavailable(
            String ticker,
            String dataStatus
    ) {
        return new WatchlistRow(
                ticker,
                LocalDate.MIN,

                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,

                "N/A",
                "N/A",
                "N/A",

                MarketRegime.NORMAL,
                dataStatus,
                false
        );
    }

    /**
     * Converts a backend SystemSignal into a display value.
     */
    private static String convertSystemStatus(
            SystemSignal signal
    ) {
        if (signal == null) {
            throw new IllegalArgumentException(
                    "System signal cannot be null"
            );
        }

        if (signal.muted()) {
            return "MUTED";
        }

        SignalDirection direction =
                signal.finalDirection();

        return switch (direction) {
            case BUY -> "BUY";
            case SHORT -> "SHORT";
            case NEUTRAL -> "NEUTRAL";
        };
    }

    /**
     * Display-friendly version of the System 4 regime.
     */
    public String marketRegimeStatus() {
        if (!dataAvailable) {
            return "N/A";
        }

        return marketRegime
                .getDisplayName()
                .toUpperCase(Locale.ROOT);
    }

    public boolean hasActiveSignal() {
        return dataAvailable
                && (isDirectional(system1Status)
                || isDirectional(system2Status)
                || isDirectional(system3Status));
    }

    public boolean hasMutedSignal() {
        return dataAvailable
                && (system1Status.equals("MUTED")
                || system2Status.equals("MUTED")
                || system3Status.equals("MUTED"));
    }

    public boolean hasWarningRegime() {
        return dataAvailable
                && (marketRegime == MarketRegime.COMPRESSION
                || marketRegime == MarketRegime.FALSE_BREAKOUT_UP
                || marketRegime == MarketRegime.FALSE_BREAKOUT_DOWN);
    }

    private static boolean isDirectional(
            String status
    ) {
        return status.equals("BUY")
                || status.equals("SHORT");
    }
}
