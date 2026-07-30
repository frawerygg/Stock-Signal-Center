package SignalEvaluator;

import Systems.SystemSignal;
import java.util.List;

public record SignalResolution(
        SystemSignal system1,
        SystemSignal system2,
        SystemSignal system3,
        MarketRegime marketRegime,
        OverallSignal overallSignal,
        int bullishCount,
        int bearishCount,
        String explanation)
{

    public SignalResolution
    {
        if (system1 == null || system2 == null || system3 == null)
        {
            throw new IllegalArgumentException("System signals cannot be null");
        }

        if (marketRegime == null) {
            throw new IllegalArgumentException(
                    "Market regime cannot be null"
            );
        }

        if (overallSignal == null) {
            throw new IllegalArgumentException(
                    "Overall signal cannot be null"
            );
        }

        if (bullishCount < 0 || bullishCount > 3) {
            throw new IllegalArgumentException(
                    "Bullish count must be between zero and three"
            );
        }

        if (bearishCount < 0 || bearishCount > 3) {
            throw new IllegalArgumentException(
                    "Bearish count must be between zero and three"
            );
        }

        if (bullishCount + bearishCount > 3) {
            throw new IllegalArgumentException(
                    "Directional counts cannot exceed three systems"
            );
        }

        if (explanation == null) {
            explanation = "";
        }
    }

    public List<SystemSignal> systems()
    {
        return List.of(system1, system2, system3);
    }

    public boolean hasAnyDirectionalSignal() {
        return bullishCount > 0
                || bearishCount > 0;
    }

    public int activeSignalCount() {
        return bullishCount + bearishCount;
    }
}