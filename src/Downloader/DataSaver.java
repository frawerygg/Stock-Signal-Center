package Downloader;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;

public class DataSaver {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path DATA_FOLDER = Path.of("stock_data");

    public static boolean save(StockInfo result) {

        if (result.getStatus() != DownloadStatus.SUCCESS) {
            System.out.println("Cannot save " + result.getTicker() + ": download was not successful.");
            return false;
        }

        if (!result.hasData()) {
            System.out.println("Cannot save " + result.getTicker() + ": data is null.");
            return false;
        }

        try {
            Files.createDirectories(DATA_FOLDER);
            Path filePath = DATA_FOLDER.resolve(result.getTicker() + ".json");

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), result.getData());

            System.out.println(result.getTicker() + " data saved successfully.");
            return true;
        }
        catch (Exception e) {
            System.out.println("Failed to save " + result.getTicker() + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean hasTodayData(String ticker)
    {
        if (ticker == null || ticker.isBlank())
        {
            return false;
        }

        String cleanedTicker = ticker.trim().toUpperCase();
        Path filepath = DATA_FOLDER.resolve(cleanedTicker + ".json");

        if (!Files.exists(filepath))
        {
            return false;
        }

        try
        {
            FileTime modifiedTime = Files.getLastModifiedTime(filepath);
            LocalDate modifiedDate = modifiedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate today = LocalDate.now();
            return modifiedDate.equals(today);
        }
        catch (Exception e)
        {
            System.out.println("There is something wrong with your time");
        }
        return false;
    }

}