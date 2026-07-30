package Downloader;

import com.fasterxml.jackson.databind.JsonNode;

public class StockInfo {
    private final String ticker;
    private final DownloadStatus status;
    private final String message;
    private final JsonNode data;

    public StockInfo(String tickers, DownloadStatus status, String message )
    {
        this.ticker = tickers;
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public StockInfo(String tickers, DownloadStatus status, String message, JsonNode data )
    {
        this.ticker = tickers;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getTicker()
    {
        return ticker;
    }
    public DownloadStatus getStatus()
    {
        return status;
    }
    public String getMessage()
    {
        return message;
    }
    public JsonNode getData()
    {
        return data;
    }
    public boolean hasData()
    {
        return data != null;
    }

}
