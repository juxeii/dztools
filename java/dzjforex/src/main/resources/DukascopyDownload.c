// Update Assets.dta and/or download price history of a certain asset (or all)
// Select the account (Demo/Real) before

//#define ADD_ASSET "EUR/USD"
// uncomment for adding a new asset to Assets.dta

//#define ALL_ASSETS
// uncomment for updating all assets in the loop (otherwise, selected asset only)

//#define PRICE_HISTORY
// uncomment for downloading the price history (otherwise, update Assets.dta only)

//#define MAX_GAP 2
// uncomment for checking the price history for 2-days gaps
#define SAVE_PATH "History"
//#define SAVE_PATH "Plugin//dukascopy//history"
// uncomment for defining the path to save the *.bar files

function run()
{
    if(!is(TRADEMODE)) {
        quit("Click [Trade] to update assets!");
        return;
    }

    LookBack = 0;
    StartDate = 2014; // set date for downloading price history
    EndDate = 2015;

#ifdef PRICE_HISTORY
#ifdef MAX_GAP
    GapDays = MAX_GAP;      // detect gaps
#endif
#else
    NumYears = -1; // don't load prices, only update Assets.dta
#endif
    string name;
#ifdef ADD_ASSET // add single asset
    name = ADD_ASSET;
    asset(name);
#else
#ifdef ALL_ASSETS // update all assets; add assets as you need
    while(name = loop("EUR/USD", "GBP/USD"))
#endif
#endif
    {
        asset(name);
        char content[100];
        sprintf(content,"Asset = %s\nStartYear = %i\nEndYear = %i\n\Path = %s", name, StartDate, EndDate, SAVE_PATH);
        file_write ("Plugin//dukascopy//HistoryConfig.properties", content, 0);
        var result = brokerCommand(666, 0);
        if(result == 0)
            quit("Error fetching history! Check logfile.");
    }
    quit("Done!");
}
