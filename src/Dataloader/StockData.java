package Dataloader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StockData {
    private final String symbol;
    private final List<DailyPrice> dailyPrices;

    public StockData(String symbol)
    {

        if (symbol == null || symbol.isBlank())
        {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }

        this.symbol = symbol.trim().toUpperCase();
        this.dailyPrices = new ArrayList<>();
    }

    public void addDailyPrice(DailyPrice dailyPrice)
    {
        if (dailyPrice == null)
        {
            throw new IllegalArgumentException("Daily price cannot be empty");
        }
        this.dailyPrices.add(dailyPrice);
    }

    public String getSymbol()
    {
        return symbol;
    }

    public List<DailyPrice> getDailyPrices()
    {
        return dailyPrices;
    }

    public int getNumberOfDays()
    {
        return dailyPrices.size();
    }

    @Override
    public String toString()
    {
        return symbol + " contains " + dailyPrices.size() + " trading days";
    }

    public void sortByDate()
    {
        dailyPrices.sort(Comparator.comparing(DailyPrice::getDate));
    }

}
