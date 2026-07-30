package SignalEvaluator;


public enum OptionStrategySignal {
    COVERED_CALL_CANDIDATE("Covered Call Candidate"),
    CASH_SECURED_PUT_CANDIDATE("Cash-Secured Put Candidate"),
    BREAKOUT_RISK("Breakout Risk"),
    NO_SIGNAL("No Option Signal");

    private final String displayName;

    OptionStrategySignal(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isTradeCandidate()
    {
        return this == COVERED_CALL_CANDIDATE || this == CASH_SECURED_PUT_CANDIDATE;
    }

    public boolean isNoSignal()
    {
        return this == NO_SIGNAL;
    }
}