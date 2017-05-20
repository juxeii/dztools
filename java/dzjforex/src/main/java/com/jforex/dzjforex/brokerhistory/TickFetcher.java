package com.jforex.dzjforex.brokerhistory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.programming.quote.TickQuote;

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
            .map(ticks -> filterTime(ticks, brokerHistoryData.startTimeForTick()))
            .map(ticks -> alignToTickQuotes(ticks, instrument))
            .doOnSuccess(brokerHistoryData::fillTickQuotes)
            .map(List::size)
            .onErrorReturnItem(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue());

        return zorro.progressWait(fetchResult);
    }

    private List<ITick> filterTime(final List<ITick> ticks,
                                   final long startDate) {
        return ticks
            .stream()
            .filter(tick -> tick.getTime() >= startDate)
            .collect(Collectors.toList());
    }

    public List<TickQuote> alignToTickQuotes(final List<ITick> ticks,
                                             final Instrument instrument) {
        return reverseQuotes(ticksToQuotes(ticks, instrument));
    }

    public List<TickQuote> ticksToQuotes(final List<ITick> ticks,
                                         final Instrument instrument) {
        return ticks
            .stream()
            .map(tick -> new TickQuote(instrument, tick))
            .collect(Collectors.toList());
    }

    private <T> List<T> reverseQuotes(final List<T> quotes) {
        Collections.reverse(quotes);
        return quotes;
    }
}
