package com.jforex.dzjforex.misc;

import com.dukascopy.api.IDataService;
import com.jforex.dzjforex.brokertime.DummySubmit;

public class MarketState {

    private final IDataService dataService;
    private final DummySubmit dummySubmit;

    public MarketState(final IDataService dataService,
                       final DummySubmit dummySubmit) {
        this.dataService = dataService;
        this.dummySubmit = dummySubmit;
    }

    public boolean isClosed(final long serverTime) {
        return dataService.isOfflineTime(serverTime)
                ? true
                : dummySubmit.wasOffline(serverTime);
    }
}
