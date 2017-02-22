package com.jforex.dzjforex.misc;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.Period;

import io.reactivex.Observable;

public class MarketData {

    private final IDataService dataService;

    private final static Logger logger = LogManager.getLogger(MarketData.class);

    public MarketData(final IDataService dataService) {
        this.dataService = dataService;
    }

    public boolean isMarketOffline(final long currentServerTime) {
        final Set<ITimeDomain> offlines = getOfflineTimes(currentServerTime,
                                                          currentServerTime + Period.ONE_MIN.getInterval());
        return offlines == null
                ? true
                : isServerTimeInOfflineDomains(currentServerTime, offlines);
    }

    private boolean isServerTimeInOfflineDomains(final long serverTime,
                                                 final Set<ITimeDomain> offlines) {
        for (final ITimeDomain offline : offlines)
            if (serverTime >= offline.getStart() && serverTime <= offline.getEnd())
                return true;
        return false;
    }

    private Set<ITimeDomain> getOfflineTimes(final long startTime,
                                             final long endTime) {
        return Observable
            .fromCallable(() -> dataService.getOfflineTimeDomains(startTime, endTime))
            .onErrorResumeNext(err -> {
                logger.error("Get market offline times  failed!" + err.getMessage());
                return Observable.just(new HashSet<>());
            })
            .blockingFirst();
    }
}
