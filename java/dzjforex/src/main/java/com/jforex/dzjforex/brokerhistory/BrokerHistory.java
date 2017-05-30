package com.jforex.dzjforex.brokerhistory;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.RxUtility;

import io.reactivex.Single;

public class BrokerHistory {

    private final BarFetcher barFetcher;
    private final TickFetcher tickFetcher;

    public BrokerHistory(final BarFetcher barFetcher,
                         final TickFetcher tickFetcher) {
        this.barFetcher = barFetcher;
        this.tickFetcher = tickFetcher;
    }

    public Single<Integer> get(final BrokerHistoryData brokerHistoryData) {
        return Single
            .defer(() -> RxUtility.instrumentFromName(brokerHistoryData.assetName()))
            .flatMap(instrument -> fetchForValidInstrument(instrument, brokerHistoryData))
            .onErrorReturnItem(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue());
    }

    private Single<Integer> fetchForValidInstrument(final Instrument instrument,
                                                    final BrokerHistoryData brokerHistoryData) {
        return brokerHistoryData.periodInMinutes() != 0
                ? barFetcher.run(instrument, brokerHistoryData)
                : tickFetcher.run(instrument, brokerHistoryData);
    }
}
