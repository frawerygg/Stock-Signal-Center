package SignalEvaluator;

import Systems.SignalDirection;
import Systems.SystemSignal;


public final class SignalResolver {

    private SignalResolver() {}

    public static SignalResolution resolve(SystemSignal system1, SystemSignal system2, SystemSignal system3, MarketRegime marketRegime)
    {
        validateInputs(system1, system2, system3, marketRegime);
        SystemSignal resolvedSystem1 = system1;
        SystemSignal resolvedSystem2 = system2;
        SystemSignal resolvedSystem3 = system3;

        switch (marketRegime)
        {

            case RANGE_BOUND ->
            {
                resolvedSystem1 = muteDirectionalSignal(system1, "Muted because the market is range-bound");
                resolvedSystem3 = muteDirectionalSignal(system3, "Muted because the market is range-bound");
            }

            case COMPRESSION ->
            {
                resolvedSystem1 = muteDirectionalSignal(system1, "Muted during volatility compression");
                resolvedSystem3 = muteDirectionalSignal(system3, "Muted during volatility compression");
            }

            case FALSE_BREAKOUT_UP ->
            {
                resolvedSystem1 = muteBullishSignal(system1, "Bullish signal rejected by false breakout");
                resolvedSystem2 = muteBullishSignal(system2, "Bullish signal rejected by false breakout");
                resolvedSystem3 = muteBullishSignal(system3, "Bullish signal rejected by false breakout");
            }

            case FALSE_BREAKOUT_DOWN ->
            {
                resolvedSystem1 = muteBearishSignal(system1, "Bearish signal rejected by false breakdown");
                resolvedSystem2 = muteBearishSignal(system2, "Bearish signal rejected by false breakdown");
                resolvedSystem3 = muteBearishSignal(system3, "Bearish signal rejected by false breakdown");
            }

            case NORMAL -> {}
        }

        int bullishCount = countDirection(SignalDirection.BUY, resolvedSystem1, resolvedSystem2, resolvedSystem3);
        int bearishCount = countDirection(SignalDirection.SHORT, resolvedSystem1, resolvedSystem2, resolvedSystem3);

        OverallSignal overallSignal = determineOverallSignal(bullishCount, bearishCount);
        String explanation = buildExplanation(marketRegime, overallSignal, bullishCount, bearishCount);

        return new SignalResolution(
                resolvedSystem1,
                resolvedSystem2,
                resolvedSystem3,
                marketRegime,
                overallSignal,
                bullishCount,
                bearishCount,
                explanation
        );
    }

    private static SystemSignal muteDirectionalSignal(SystemSignal signal, String reason)
    {
        if (!signal.finalDirection().isDirectional())
        {
            return signal;
        }
        return signal.mute(reason);
    }

    private static SystemSignal muteBullishSignal(SystemSignal signal, String reason)
    {
        if (signal.finalDirection() != SignalDirection.BUY)
        {
            return signal;
        }
        return signal.mute(reason);
    }

    private static SystemSignal muteBearishSignal(SystemSignal signal, String reason)
    {
        if (signal.finalDirection() != SignalDirection.SHORT)
        {
            return signal;
        }
        return signal.mute(reason);
    }

    private static int countDirection(SignalDirection direction, SystemSignal... signals)
    {
        int count = 0;
        for (SystemSignal signal : signals)
        {
            if (signal.finalDirection() == direction) {
                count++;
            }
        }
        return count;
    }


    private static OverallSignal determineOverallSignal(int bullishCount, int bearishCount)
    {
        if (bullishCount == 0 && bearishCount == 0)
        {
            return OverallSignal.NEUTRAL;
        }

        if (bullishCount > 0 && bearishCount > 0)
        {
            if (bullishCount == bearishCount)
            {
                return OverallSignal.MIXED;
            }
            if (bullishCount > bearishCount)
            {
                return OverallSignal.WEAK_BUY;
            }
            return OverallSignal.WEAK_SHORT;
        }

        if (bullishCount > 0)
        {
            return switch (bullishCount)
            {
                case 1 -> OverallSignal.WEAK_BUY;
                case 2 -> OverallSignal.BUY;
                case 3 -> OverallSignal.STRONG_BUY;
                default -> throw new IllegalStateException("Unexpected bullish count: " + bullishCount);
            };
        }

        return switch (bearishCount)
        {
            case 1 -> OverallSignal.WEAK_SHORT;
            case 2 -> OverallSignal.SHORT;
            case 3 -> OverallSignal.STRONG_SHORT;
            default -> throw new IllegalStateException("Unexpected bearish count: " + bearishCount);
        };
    }

    private static String buildExplanation(MarketRegime regime, OverallSignal overallSignal, int bullishCount, int bearishCount)
    {
        String voteText = bullishCount + " bullish and " + bearishCount + " bearish active signals";

        return switch (regime) {
            case NORMAL -> overallSignal.getDisplayName() + ": " + voteText;
            case RANGE_BOUND -> overallSignal.getDisplayName() + ": trend and pullback systems were " + "muted in a range-bound market; " + voteText;
            case COMPRESSION -> overallSignal.getDisplayName() + ": trend and pullback systems were " + "muted during volatility compression; " + voteText;
            case FALSE_BREAKOUT_UP -> overallSignal.getDisplayName() + ": bullish signals were rejected " + "after a false upward breakout; " + voteText;
            case FALSE_BREAKOUT_DOWN -> overallSignal.getDisplayName() + ": bearish signals were rejected " + "after a false downward breakdown; " + voteText;
        };
    }

    private static void validateInputs(
            SystemSignal system1,
            SystemSignal system2,
            SystemSignal system3,
            MarketRegime marketRegime
    )
    {
        if (system1 == null
                || system2 == null
                || system3 == null) {
            throw new IllegalArgumentException(
                    "System signals cannot be null"
            );
        }

        if (marketRegime == null) {
            throw new IllegalArgumentException(
                    "Market regime cannot be null"
            );
        }

        if (system1.systemNumber() != 1) {
            throw new IllegalArgumentException(
                    "First argument must be System 1"
            );
        }

        if (system2.systemNumber() != 2) {
            throw new IllegalArgumentException(
                    "Second argument must be System 2"
            );
        }

        if (system3.systemNumber() != 3) {
            throw new IllegalArgumentException(
                    "Third argument must be System 3"
            );
        }
    }
}