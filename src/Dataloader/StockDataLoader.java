package Dataloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StockDataLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static StockData loadStocker(Path filePath)
        throws IOException
    {
        JsonNode root = mapper.readTree(filePath.toFile());

        JsonNode metaData = root.path("Meta Data");
        String symbol = metaData.path("2. Symbol").asText();

        if (symbol.isBlank())
        {
            throw new IllegalArgumentException("Symbole was not found in: "+ filePath);
        }

        JsonNode timeSeries = root.path("Time Series (Daily)");

        if (timeSeries.isMissingNode() || timeSeries.isEmpty())
        {
            throw new IllegalArgumentException("Time series data was not found in: "+ filePath);
        }

        StockData stockData = new StockData(symbol);
        Iterator<Map.Entry<String, JsonNode>> dates = timeSeries.fields();

        while (dates.hasNext())
        {
            Map.Entry<String, JsonNode> entry = dates.next();

            String dateText = entry.getKey();
            JsonNode priceData = entry.getValue();

            LocalDate date = LocalDate.parse(dateText);

            double open = priceData.path("1. open").asDouble();
            double high = priceData.path("2. high").asDouble();
            double low = priceData.path("3. low").asDouble();
            double close = priceData.path("4. close").asDouble();
            long volume = priceData.path("5. volume").asLong();

            DailyPrice dailyPrice = new DailyPrice(date, open, high, low, close, volume);
            stockData.addDailyPrice(dailyPrice);
        }
        stockData.sortByDate();
        return stockData;

    }

    public static HashMap<String, StockData> loadAllStock(Path folderPath)
            throws IOException
    {
        HashMap <String, StockData> stockmap = new HashMap<>();

        if (!Files.exists(folderPath))
        {
            throw new IllegalArgumentException("Stock folder does not exist");
        }

        if (!Files.isDirectory(folderPath))
        {
            throw new IllegalArgumentException("The path is not a folder");
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(folderPath)){
            for (Path filePath: files)
            {
                String fileName = filePath.getFileName().toString().toLowerCase();

                if (!Files.isRegularFile(filePath)||!fileName.endsWith(".json"))
                {
                    continue;
                }

                try {
                    StockData stockData = loadStocker(filePath);
                    stockmap.put(stockData.getSymbol(), stockData);
                    System.out.println("Load " + stockData.getSymbol() + " with " + stockData.getNumberOfDays() + " Trading days");
                }
                catch (IOException | IllegalArgumentException exception)
                {
                    System.out.println("Skipped "+ filePath.getFileName());
                }

            }
        }
        return stockmap;
    }
}
