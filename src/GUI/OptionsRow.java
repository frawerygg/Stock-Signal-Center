package GUI;

import MetricCalculator.SignalMetrics;
import SignalEvaluator.MarketRegime;
import SignalEvaluator.OptionStrategySignal;
import SignalEvaluator.StockSignalResult;

import java.time.LocalDate;
import java.util.Locale;

/**
 * One display row for the Options Income page.
 *
 * This record does not evaluate options.
 * It only extracts the existing OptionStrategyEvaluation
 * from StockSignalResult.
 */
public record OptionsRow(
        String ticker,
        LocalDate date,

        OptionStrategySignal strategySignal,
        MarketRegime marketRegime,

        double price,
        double sma20,
        double rsi14,
        double relativeVolume20,

        double bollingerLower20,
        double bollingerUpper20,
        double bollingerPosition,

        int activeDirectionalSignalCount,

        String explanation
) {
    public OptionsRow {
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

        if (strategySignal == null) {
            throw new IllegalArgumentException(
                    "Option strategy signal cannot be null"
            );
        }

        if (marketRegime == null) {
            throw new IllegalArgumentException(
                    "Market regime cannot be null"
            );
        }

        if (explanation == null) {
            explanation = "";
        }
    }

    /**
     * Input:
     * One complete StockSignalResult.
     *
     * Output:
     * One GUI-ready OptionsRow.
     */
    public static OptionsRow from(
            StockSignalResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Stock signal result cannot be null"
            );
        }

        SignalMetrics metrics =
                result.metrics();

        double bandRange =
                metrics.bollingerUpper20()
                        - metrics.bollingerLower20();

        double bollingerPosition;

        if (bandRange <= 0.0) {
            bollingerPosition = 0.50;
        } else {
            bollingerPosition =
                    (metrics.close()
                            - metrics.bollingerLower20())
                            / bandRange;
        }

        return new OptionsRow(
                result.ticker(),
                result.date(),

                result.optionSignal(),
                result.marketRegime(),

                metrics.close(),
                metrics.sma20(),
                metrics.rsi14(),
                metrics.relativeVolume20(),

                metrics.bollingerLower20(),
                metrics.bollingerUpper20(),
                bollingerPosition,

                result.activeSignalCount(),

                result.optionEvaluation()
                        .explanation()
        );
    }

    public String strategyDisplayName() {
        return strategySignal.getDisplayName();
    }

    public String regimeDisplayName() {
        return marketRegime
                .getDisplayName()
                .toUpperCase(Locale.ROOT);
    }

    public boolean isCandidate() {
        return strategySignal.isTradeCandidate();
    }

    public boolean isCoveredCallCandidate() {
        return strategySignal
                == OptionStrategySignal
                .COVERED_CALL_CANDIDATE;
    }

    public boolean isCashSecuredPutCandidate() {
        return strategySignal
                == OptionStrategySignal
                .CASH_SECURED_PUT_CANDIDATE;
    }

    public boolean isBreakoutRisk() {
        return strategySignal
                == OptionStrategySignal
                .BREAKOUT_RISK;
    }

    public boolean hasNoSignal() {
        return strategySignal
                == OptionStrategySignal
                .NO_SIGNAL;
    }

    public double bollingerPositionPercent() {
        return bollingerPosition * 100.0;
    }
}