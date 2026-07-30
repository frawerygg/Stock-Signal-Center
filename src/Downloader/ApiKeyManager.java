package Downloader;

import java.util.ArrayList;
import java.util.List;

public class ApiKeyManager {

    private final List<String> apiKEYS;
    private  int currentindex;

    public ApiKeyManager( List<String> apiKEYS)
    {
        if (apiKEYS == null || apiKEYS.isEmpty())
        {
            throw new IllegalArgumentException("At least one API is required");
        }
        this.apiKEYS= new ArrayList<>();

        for ( String apiKEY : apiKEYS )
        {
            if (apiKEY != null && ! apiKEY.isBlank() )
            {
                this.apiKEYS.add(apiKEY.trim());
            }

            if (this.apiKEYS.isEmpty())
            {
                throw new IllegalArgumentException("All provided API keys are empty");
            }

            this.currentindex=0;
        }
    }

    public String getCurrentKey()
    {
        if (! hasAvailableKey ()) {
            throw new IllegalStateException(
                    "No API keys are available."
            );
        }
        return apiKEYS.get(currentindex);
    }

    public boolean moveToNextKey()
    {
        currentindex ++;
        return hasAvailableKey();
    }

    public boolean hasAvailableKey()
    {
        return currentindex < apiKEYS.size();
    }

    public int getCurrentindex()
    {
        if (!hasAvailableKey())
        {
            return 0;
        }
        return currentindex +1;
    }

    public int getTotalKetCount()
    {
        return apiKEYS.size();
    }

    public int getRemainingKeyCount()
    {
        return Math.max(0, apiKEYS.size()-currentindex);
    }

}
