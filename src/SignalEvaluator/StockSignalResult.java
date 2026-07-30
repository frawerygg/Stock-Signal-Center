package SignalEvaluator;
import MetricCalculator.SignalMetrics;
import Systems.SystemSignal;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Complete signal-engine result for one stock on one trading day.
 *
 * This object is suitable for:
 * - Console testing
 * - JavaFX tables
 * - Detail panels
 * - Signal cards
 */
public record StockSignalResult(
        SignalMetrics metrics,
        SignalResolution resolution,
        OptionStrategyEvaluation optionEvaluation
) {

    public StockSignalResult {
        if (metrics == null) {
            throw new IllegalArgumentException(
                    "Signal metrics cannot be null"
            );
        }

        if (resolution == null) {
            throw new IllegalArgumentException(
                    "Signal resolution cannot be null"
            );
        }

        if (optionEvaluation == null) {
            throw new IllegalArgumentException(
                    "Option evaluation cannot be null"
            );
        }
    }

    // ------------------------------------------------------------
    // Basic stock information
    // ------------------------------------------------------------

    public String ticker() {
        return metrics.ticker();
    }

    public LocalDate date() {
        return metrics.date();
    }

    public double price() {
        return metrics.close();
    }

    // ------------------------------------------------------------
    // Overall results
    // ------------------------------------------------------------

    public OverallSignal overallSignal() {
        return resolution.overallSignal();
    }

    public MarketRegime marketRegime() {
        return resolution.marketRegime();
    }

    public OptionStrategySignal optionSignal() {
        return optionEvaluation.signal();
    }

    public String overallDisplayName() {
        return overallSignal().getDisplayName();
    }

    public String regimeDisplayName() {
        return marketRegime().getDisplayName();
    }

    public String optionDisplayName() {
        return optionSignal().getDisplayName();
    }

    // ------------------------------------------------------------
    // Individual systems
    // ------------------------------------------------------------

    public SystemSignal system1() {
        return resolution.system1();
    }

    public SystemSignal system2() {
        return resolution.system2();
    }

    public SystemSignal system3() {
        return resolution.system3();
    }

    public List<SystemSignal> systems() {
        return resolution.systems();
    }

    // ------------------------------------------------------------
    // Convenience methods for the future GUI
    // ------------------------------------------------------------

    public int bullishCount() {
        return resolution.bullishCount();
    }

    public int bearishCount() {
        return resolution.bearishCount();
    }

    public int activeSignalCount() {
        return resolution.activeSignalCount();
    }

    public boolean hasDirectionalSignal() {
        return resolution.hasAnyDirectionalSignal();
    }

    public boolean hasOptionCandidate() {
        return optionEvaluation.isCandidate();
    }

    public boolean hasBreakoutRisk() {
        return optionEvaluation.hasBreakoutRisk();
    }

    public boolean isBullish() {
        return overallSignal().isBullish();
    }

    public boolean isBearish() {
        return overallSignal().isBearish();
    }

    public boolean isNeutral() {
        return overallSignal() == OverallSignal.NEUTRAL;
    }

    public boolean isMixed() {
        return overallSignal() == OverallSignal.MIXED;
    }

    /**
     * Summary suitable for a JavaFX detail panel.
     */
    public String getSummaryText() {
        return String.format(
                Locale.US,
                "%s | %s | $%.2f | Overall: %s | "
                        + "Regime: %s | Options: %s",
                ticker(),
                date(),
                price(),
                overallDisplayName(),
                regimeDisplayName(),
                optionDisplayName()
        );
    }
}