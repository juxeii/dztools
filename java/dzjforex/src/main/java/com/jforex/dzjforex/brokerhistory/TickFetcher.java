package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import com.dukascopy.api.Instrument;
import com.jforex.programming.quote.TickQuote;

import io.reactivex.Single;

public class TickFetcher {

    private final TickHistoryByShift tickHistoryByShift;

    public TickFetcher(final TickHistoryByShift tickHistoryByShift) {
        this.tickHistoryByShift = tickHistoryByShift;
    }

    public Single<Integer> run(final Instrument instrument,
                               final BrokerHistoryData brokerHistoryData) {
        return Single
            .defer(() -> tickHistoryByShift.get(instrument,
                                                brokerHistoryData.endTimeForTick(),
                                                brokerHistoryData.noOfRequestedTicks() - 1))
            .flattenAsObservable(bars -> bars)
            .filter(tick -> tick.getTime() >= brokerHistoryData.startTimeForTick())
            .map(tick -> new TickQuote(instrument, tick))
            .toList()
            .doOnSuccess(brokerHistoryData::fillTickQuotes)
            .map(List::size);
    }
}
