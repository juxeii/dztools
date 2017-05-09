package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;

import io.reactivex.Single;

public class TickFetcher {

    private final HistoryProvider historyProvider;
    private final Zorro zorro;

    public TickFetcher(final HistoryProvider historyProvider,
                       final Zorro zorro) {
        this.historyProvider = historyProvider;
        this.zorro = zorro;
    }

    public int run(final Instrument instrument,
                   final BrokerHistoryData brokerHistoryData) {
        final Single<Integer> fetchResult = historyProvider
            .ticksByShift(instrument,
                          brokerHistoryData.endTimeForTick(),
                          brokerHistoryData.noOfRequestedTicks() - 1)
            .flattenAsObservable(ticks -> ticks)
            .filter(tickQuote -> tickQuote
                .tick()
                .getTime() >= brokerHistoryData.startTimeForTick())
            .toList()
            .doOnSuccess(brokerHistoryData::fillTickQuotes)
            .map(List::size)
            .onErrorReturnItem(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue());

        return zorro.progressWait(fetchResult);
    }
}
