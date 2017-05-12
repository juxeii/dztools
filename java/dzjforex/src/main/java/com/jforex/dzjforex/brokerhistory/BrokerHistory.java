package com.jforex.dzjforex.brokerhistory;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.RxUtility;

public class BrokerHistory {

    private final BarFetcher barFetcher;
    private final TickFetcher tickFetcher;

    public BrokerHistory(final BarFetcher barFetcher,
                         final TickFetcher tickFetcher) {
        this.barFetcher = barFetcher;
        this.tickFetcher = tickFetcher;
    }

    public int get(final BrokerHistoryData brokerHistoryData) {
        return RxUtility
            .instrumentFromName(brokerHistoryData.instrumentName())
            .map(instrument -> getForValidInstrument(instrument, brokerHistoryData))
            .onErrorReturnItem(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue())
            .blockingGet();
    }

    private int getForValidInstrument(final Instrument instrument,
                                      final BrokerHistoryData brokerHistoryData) {
        return brokerHistoryData.noOfTickMinutes() != 0
                ? barFetcher.run(instrument, brokerHistoryData)
                : tickFetcher.run(instrument, brokerHistoryData);
    }
}
