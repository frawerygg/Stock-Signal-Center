package Systems;

public record SystemSignal(
        int systemNumber,
        String systemName,
        SignalDirection rawDirection,
        SignalDirection finalDirection,
        double stopPrice,
        boolean muted,
        String reason)
{

    public SystemSignal
    {
        if (systemNumber <= 0)
        {
            throw new IllegalArgumentException("System number must be greater than zero");
        }

        if (systemName == null || systemName.isBlank())
        {
            throw new IllegalArgumentException("System name cannot be blank");
        }

        if (rawDirection == null)
        {
            throw new IllegalArgumentException("Raw direction cannot be null");
        }

        if (finalDirection == null)
        {
            throw new IllegalArgumentException("Final direction cannot be null");
        }

        if (Double.isInfinite(stopPrice))
        {
            throw new IllegalArgumentException("Stop price cannot be infinite");
        }

        if (!Double.isNaN(stopPrice) && stopPrice <= 0.0)
        {
            throw new IllegalArgumentException("Stop price must be positive");
        }

        if (muted && finalDirection != SignalDirection.NEUTRAL)
        {
            throw new IllegalArgumentException("A muted signal must have a neutral final direction");
        }

        if (muted && rawDirection == SignalDirection.NEUTRAL)
        {
            throw new IllegalArgumentException("A neutral raw signal cannot be muted");
        }

        if (!muted && rawDirection != finalDirection)
        {
            throw new IllegalArgumentException("An unmuted final direction must match its raw direction");
        }

        if (reason == null)
        {
            reason = "";
        }
    }

    public static SystemSignal active(int systemNumber, String systemName, SignalDirection direction, double stopPrice, String reason)
    {
        if (direction == SignalDirection.NEUTRAL)
        {
            throw new IllegalArgumentException("Use SystemSignal.neutral() for a neutral result");
        }

        return new SystemSignal(systemNumber, systemName, direction, direction, stopPrice, false, reason);
    }


    public static SystemSignal neutral(int systemNumber, String systemName, String reason)
    {
        return new SystemSignal(systemNumber, systemName, SignalDirection.NEUTRAL, SignalDirection.NEUTRAL, Double.NaN, false, reason);
    }

    public SystemSignal mute(String muteReason)
    {
        if (!rawDirection.isDirectional())
        {
            return this;
        }

        String finalReason = muteReason == null || muteReason.isBlank() ? "Signal muted by the final resolver" : muteReason;
        return new SystemSignal(systemNumber, systemName, rawDirection, SignalDirection.NEUTRAL, stopPrice, true, finalReason);
    }
}