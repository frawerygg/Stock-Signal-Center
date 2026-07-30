    package SignalEvaluator;


    public record OptionStrategyEvaluation(
            OptionStrategySignal signal,
            String explanation)
    {

        public OptionStrategyEvaluation
        {
            if (signal == null)
            {
                throw new IllegalArgumentException("Option strategy signal cannot be null");
            }

            if (explanation == null)
            {
                explanation = "";
            }
        }

        public boolean isCandidate()
        {
            return signal.isTradeCandidate();
        }

        public boolean hasBreakoutRisk()
        {
            return signal == OptionStrategySignal.BREAKOUT_RISK;
        }

        public String getDisplayName()
        {
            return signal.getDisplayName();
        }
    }