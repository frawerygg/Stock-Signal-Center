package SignalEvaluator;


public enum MarketRegime {

    NORMAL("Normal"),
    RANGE_BOUND("Range Bound"),
    COMPRESSION("Volatility Compression"),
    FALSE_BREAKOUT_UP("False Bullish Breakout"),
    FALSE_BREAKOUT_DOWN("False Bearish Breakdown");

    private final String displayName;

    MarketRegime(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

}