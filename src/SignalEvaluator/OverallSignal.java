package SignalEvaluator;


public enum OverallSignal {

    STRONG_BUY("Strong Buy"),
    BUY("Buy"),
    WEAK_BUY("Weak Buy"),

    MIXED("Mixed"),
    NEUTRAL("Neutral"),

    WEAK_SHORT("Weak Short"),
    SHORT("Short"),
    STRONG_SHORT("Strong Short");

    private final String displayName;

    OverallSignal(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isBullish() {
        return this == STRONG_BUY
                || this == BUY
                || this == WEAK_BUY;
    }

    public boolean isBearish() {
        return this == STRONG_SHORT
                || this == SHORT
                || this == WEAK_SHORT;
    }

    public boolean isActionable() {
        return isBullish() || isBearish();
    }
}