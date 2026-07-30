package Systems;

public enum SignalDirection {

    BUY("Buy"),
    SHORT("Sell / Short"),
    NEUTRAL("Neutral");

    private final String displayName;

    SignalDirection(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isDirectional()
    {
        return this == BUY || this == SHORT;
    }

}