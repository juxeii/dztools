package com.jforex.dzjforex.brokerhistory;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.history.TickHistoryByShift;
import com.jforex.programming.quote.TickQuote;

import io.reactivex.Single;

public class TickFetcher {

    private final TickHistoryByShift tickHistoryByShift;

    public TickFetcher(final TickHistoryByShift tickHistoryByShift) {
        this.tickHistoryByShift = tickHistoryByShift;
    }

    public Single<Integer> run(final Instrument instrument,
                               final BrokerHistoryData brokerHistoryData) {
        return tickHistoryByShift
            .get(instrument,
                 brokerHistoryData.endTimeForTick(),
                 brokerHistoryData.noOfRequestedTicks() - 1)
            .map(ticks -> filterTime(ticks, brokerHistoryData.startTimeForTick()))
            .map(ticks -> ticksToQuotes(ticks, instrument))
            .doOnSuccess(brokerHistoryData::fillTickQuotes)
            .map(List::size);
    }

    private List<ITick> filterTime(final List<ITick> ticks,
                                   final long startDate) {
        return ticks
            .stream()
            .filter(tick -> tick.getTime() >= startDate)
            .collect(Collectors.toList());
    }

    private List<TickQuote> ticksToQuotes(final List<ITick> ticks,
                                          final Instrument instrument) {
        return ticks
            .stream()
            .map(tick -> new TickQuote(instrument, tick))
            .collect(Collectors.toList());
    }
}
