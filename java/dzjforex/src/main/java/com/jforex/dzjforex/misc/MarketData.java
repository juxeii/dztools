package com.jforex.dzjforex.misc;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.Period;

import io.reactivex.Single;

public class MarketData {

    private final IDataService dataService;

    private final static Logger logger = LogManager.getLogger(MarketData.class);

    public MarketData(final IDataService dataService) {
        this.dataService = dataService;
    }

    public boolean isMarketOffline(final long currentServerTime) {
        final long lookUpEndTime = currentServerTime + Period.ONE_MIN.getInterval();

        return Single
            .fromCallable(() -> dataService.getOfflineTimeDomains(currentServerTime, lookUpEndTime))
            .map(domains -> isServerTimeInOfflineDomains(currentServerTime, domains))
            .doOnError(e -> logger.error("Get market offline times  failed!" + e.getMessage()))
            .onErrorReturnItem(true)
            .blockingGet();
    }

    private boolean isServerTimeInOfflineDomains(final long serverTime,
                                                 final Set<ITimeDomain> offlineDomains) {
        return offlineDomains
            .stream()
            .anyMatch(timeDomain -> serverTime >= timeDomain.getStart() && serverTime <= timeDomain.getEnd());
    }
}
