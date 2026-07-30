# Stock Signal Center

<p align="center">
  <img
    width="1036"
    height="652"
    alt="Stock Signal Center application preview"
    src="https://github.com/user-attachments/assets/43685ba5-7b81-40ba-a935-024abe15df13"
  />
</p>

Stock Signal Center is a JavaFX desktop application that downloads historical stock-market data, calculates technical indicators locally, evaluates rule-based trading systems, and displays the results through an interactive desktop interface.

## Download and Run

The easiest way to run the application is to download the latest prebuilt Windows release:

[**Download the latest Windows release**](https://github.com/frawerygg/Stock-Signal-Center/releases/latest)

After downloading:

1. Extract the entire ZIP file.
2. Keep all included files and folders together.
3. Double-click `Stock Signal Center.bat`.
4. Install Java 25 or newer if Java is not already available on your computer.

The release package includes the required JavaFX and Jackson libraries.

## How It Works

Stock Signal Center follows a local data-processing workflow:

1. Downloads daily stock-market data from the Alpha Vantage API.
2. Stores the downloaded responses as local JSON files.
3. Calculates technical indicators locally in Java.
4. Evaluates each stock using multiple rule-based systems.
5. Applies market-regime and signal-resolution logic.
6. Displays signals, metrics, stops, and historical results in the JavaFX interface.

Once market data has been downloaded, the indicator calculations and signal evaluations are performed locally.

## Main Features

- Custom stock watchlist management
- Alpha Vantage market-data integration
- Automatic API-key rotation
- Local JSON data storage
- Technical-indicator calculations
- Buy and short signal detection
- Historical signal review
- Market-regime filtering
- Protective-stop calculations
- Exit and sell-point analysis
- Option-income candidate screening
- Interactive JavaFX desktop interface
- Integrated application log console

## Trading Systems

### System 1 — Moving Average Crossover

Identifies potential trend changes using the relationship between the 20-day and 50-day simple moving averages.

The system also considers factors such as price position, RSI, moving-average direction, and recent price movement before confirming a signal.

### System 2 — Breakout

Identifies stocks moving beyond an established historical price range.

Breakout signals use price and volume confirmation to reduce the likelihood of treating weak or low-volume movements as valid breakouts.

### System 3 — Pullback and Recovery

Looks for stocks that experienced a meaningful decline or pullback and then began to recover.

The system evaluates recovery conditions using price behavior, RSI, trend direction, moving-average slope, and protective-stop levels.

### Option Income System

Screens stocks for possible covered-call and cash-secured-put candidates.

The system evaluates conditions such as:

- Bollinger Band position
- RSI range
- Relative volume
- Moving-average smoothness
- Distance between the 20-day and 50-day moving averages

This system identifies possible candidates only. It does not evaluate option-chain premiums, expiration dates, strike prices, or implied volatility.

## Technical Indicators

The application calculates several technical indicators locally, including:

- SMA 20
- SMA 50
- RSI 14
- ATR 14
- Bollinger Bands
- Bollinger Band width
- Relative volume
- Historical highs and lows
- Moving-average slopes
- Daily percentage change
- Drawdown and recovery measurements

## Technology Stack

- Java
- JavaFX
- Jackson JSON library
- Alpha Vantage API
- IntelliJ IDEA
- Local JSON file storage
- Object-oriented application architecture
