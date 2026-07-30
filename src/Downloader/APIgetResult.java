package Downloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class APIgetResult {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String symbol;
    private final String apiKEy;


    public APIgetResult(String symbol, String apiKEY)
    {
        if (symbol == null ||symbol.isBlank())
        {
            throw new IllegalArgumentException("Ticker can not be empty");
        }

        if (apiKEY == null ||apiKEY.isBlank())
        {
            throw new IllegalArgumentException("apikey can not be empty");
        }

        this.symbol = symbol.trim().toUpperCase();
        this.apiKEy = apiKEY.trim();
    }


    public StockInfo downloadResult()
    {
        try {
            Thread.sleep(1050);
        }
        catch (Exception e)
        {
            System.out.println("went wrong");
        }
        String url = "https://www.alphavantage.co/query?"
                + "function=TIME_SERIES_DAILY"
                + "&symbol=" + symbol
                + "&outputsize=compact"
                + "&apikey=" + apiKEy;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                return new StockInfo(symbol, DownloadStatus.REQUEST_FAILED, "HTTP error: " + response.statusCode());
            }

            String body = response.body();
            JsonNode root = mapper.readTree(body);

            if ( root == null || root.isEmpty())
            {
                return new StockInfo(symbol, DownloadStatus.REQUEST_FAILED, "The API returns an empty response");
            }

            JsonNode timeSeries = root.get("Time Series (Daily)");

            if (timeSeries !=null && timeSeries.isObject() && !timeSeries.isEmpty())
            {
                return new StockInfo(symbol, DownloadStatus.SUCCESS, "Valid dailt stock data recieved", root);
            }

            if (root.has("Error Message"))
            {
                return new StockInfo(symbol, DownloadStatus.INVALID_TICKERS, "INVALID");
            }

            String apiMessage = getApiMessage(root);

            if(apiMessage !=null)
            {
                if (isQuoteMessage(apiMessage))
                {
                    return new StockInfo(symbol, DownloadStatus.QUOTA_EXCEED, apiMessage);
                }

                    return new StockInfo(symbol, DownloadStatus.REQUEST_FAILED, apiMessage);
            }
                return new StockInfo(symbol, DownloadStatus.REQUEST_FAILED, "Unregonzied API response");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new StockInfo(symbol, DownloadStatus.REQUEST_FAILED,e.getMessage());
        }
    }

    private boolean isQuoteMessage(String message)
    {
        String lowerMessage = message.toLowerCase();

        return lowerMessage.contains("rate limit") ||
                lowerMessage.contains("daily rate limits")||
                lowerMessage.contains("25 requests")||
                lowerMessage.contains("premium plans");
    }

    private String getApiMessage(JsonNode root)
    {
        if (root.hasNonNull("Information"))
        {
            return root.get("Information").asText();
        }
        if (root.hasNonNull("Note"))
        {
            return root.get("Note").asText();
        }
        return null;
    }

}
