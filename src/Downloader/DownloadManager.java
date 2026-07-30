package Downloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class DownloadManager {

    private final List<String> tickers;
    private final ApiKeyManager apiKeyManager;
    private final LinkedHashMap<String, StockInfo> downloadResult;

    public DownloadManager( List<String>  tickers, ApiKeyManager apiKeyManager )
    {
        if (tickers == null)
        {
            throw new IllegalArgumentException("Ticker cannot be null");
        }

        if (apiKeyManager == null)
        {
            throw new IllegalArgumentException("API KEY cannot be null");
        }
        this.tickers = new ArrayList<>(tickers);
        this.apiKeyManager = apiKeyManager;
        this.downloadResult= new LinkedHashMap<>();
        initializePendingResults();
    }

    private void initializePendingResults()
    {
        for ( String ticker : tickers)
        {
            StockInfo pendingResult = new StockInfo(ticker, DownloadStatus.PENDING, "Waiting to be Download");
            StockInfo savedResult = new StockInfo(ticker, DownloadStatus.ALREADY_SAVED, "Saved already");

            if ( !DataSaver.hasTodayData(ticker))
            {
                downloadResult.put(ticker, pendingResult);
            }

            else
            {
                downloadResult.put(ticker, savedResult);
            }

        }
    }

    public void downloadAll()
    {
        for (String ticker: tickers) {
            if (!DataSaver.hasTodayData(ticker)) {
                boolean tickerFinished = false;

                while (!tickerFinished && apiKeyManager.hasAvailableKey()) {
                    System.out.println(
                            "Downloading "
                                    + ticker
                                    + " using API key "
                                    + apiKeyManager.getCurrentindex()
                                    + " of "
                                    + apiKeyManager.getTotalKetCount()
                    );
                    APIgetResult api = new APIgetResult(ticker, apiKeyManager.getCurrentKey());
                    StockInfo result = api.downloadResult();

                    if (result.getStatus() == DownloadStatus.QUOTA_EXCEED) {
                        System.out.println("API QUOTDA exceed while requesting" + ticker);
                        boolean anotherKeyAvaliable = apiKeyManager.moveToNextKey();
                        System.out.println(result.getMessage());

                        if (anotherKeyAvaliable) {
                            System.out.println("Switching API key and retrying " + ticker);
                            continue;
                        }
                        System.out.println("All API keys have exceed their quotas");
                        break;
                    }
                    result = saveSuccessfulResult(result);

                    downloadResult.put(ticker, result);
                    tickerFinished = true;
                }

                if (!tickerFinished && !apiKeyManager.hasAvailableKey()) {
                    System.out.println("Stop downloas " + ticker + " and the remaining tickers are pending");
                    break;
                }
            }

            else
            {
                System.out.println(ticker +" has already been saved today");
            }
        }
    }

    public LinkedHashMap<String, StockInfo> getDownloadResult()
    {
        return new LinkedHashMap<>(downloadResult);
    }

    public void printResults()
    {
        System.out.println();
        System.out.println("DOWNLOADRESULTS");
        System.out.println("+++++++++++++++++");

        for (StockInfo result : downloadResult.values())
        {
            System.out.println("Ticker: "+ result.getTicker());
            System.out.println("Status "+result.getStatus());
            System.out.println("Message "+result.getMessage());
            System.out.println("Has data "+ result.hasData());
            System.out.println("----------------------------");
        }
    }

    private StockInfo saveSuccessfulResult(StockInfo result)
    {
        if (result.getStatus() != DownloadStatus.SUCCESS)
        {
            return result;
        }

        boolean savedSuccessfullt = DataSaver.save(result);
        if (savedSuccessfullt)
        {
            return result;
        }
        return new StockInfo(result.getTicker(), DownloadStatus.SAVE_FAILED,"API Succeed, but file cannot be saved", result.getData());
    }

}
