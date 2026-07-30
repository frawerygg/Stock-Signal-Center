import GUI.StartupSplash;
import GUI.StockSignalApplication;

public class Main
{
    public static void main(String[] args)
    {
        StartupSplash.showSplash();

        try
        {
            StockSignalApplication.main(args);
        }
        finally
        {
            StartupSplash.closeSplash();
        }
    }
}