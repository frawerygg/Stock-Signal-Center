package Downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StockTickerList {
    private final Path tickerFile =Path.of("Ticker.txt");;
    private final ArrayList<String> stockList;

    public StockTickerList() throws IOException
    {
        this.stockList = loadStock(tickerFile);
    }
//============================================================================================================//
//============================================================================================================//
//============================================================================================================//
    public ArrayList<String> loadStock (Path path)
            throws IOException
    {

        ArrayList<String> stockList = new ArrayList<>();

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        List<String> lines = Files.readAllLines(path);
        for( String line : lines)
        {
            String ticker = line.trim().toUpperCase();
            if (!ticker.isEmpty() && !stockList.contains(ticker))
            {
                stockList.add(ticker);
            }
        }
        return stockList;
    }
//============================================================================================================//
//============================================================================================================//
//============================================================================================================//
    private void writeFiles(ArrayList<String> list, Path path)
            throws IOException
    {
        Files.write(path, list);
    }
//============================================================================================================//
//============================================================================================================//
//============================================================================================================//
    public boolean addTicker(String newTicker)
            throws IOException
    {
        String cleanTicker = newTicker.trim().toUpperCase();
        if (cleanTicker.isEmpty())
        {
            System.out.println("Ticker is empty");
            return false;
        }
        if (stockList.contains(cleanTicker))
        {
            System.out.println(cleanTicker + " already exist");
            return false;
        }
        stockList.add(cleanTicker);
        writeFiles(stockList, tickerFile);
        return true;
    }
//============================================================================================================//
//============================================================================================================//
//============================================================================================================//
    public boolean removeTicker(String removeTicker)
            throws IOException
    {
        String cleanedTicker = removeTicker.trim().toUpperCase();
        boolean wasRemoved = stockList.remove(cleanedTicker);

        if (wasRemoved)
        {
            writeFiles(stockList, tickerFile);
            System.out.println("Removed successfully");
            return true;
        }
        System.out.println("Not in the List");
        return false;
    }
//============================================================================================================//
//============================================================================================================//
//============================================================================================================//
    public ArrayList<String> getQuote()
    {
        return new ArrayList<>(stockList);
    }

}
