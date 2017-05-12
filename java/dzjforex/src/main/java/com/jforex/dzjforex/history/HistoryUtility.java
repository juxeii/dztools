package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryUtility {

    private final HistoryWrapper historyWrapper;
    private final long tickFetchMillis;

    public HistoryUtility(final HistoryWrapper historyWrapper,
                          final PluginConfig pluginConfig) {
        this.historyWrapper = historyWrapper;

        tickFetchMillis = pluginConfig.tickFetchMillis();
    }

    public Observable<List<ITick>> ticksByShiftAdapted(final Instrument instrument,
                                                       final long endDate,
                                                       final int shift) {
        return Single
            .defer(() -> adaptTickFetchEndTime(instrument, endDate))
            .flatMapObservable(this::countStreamForTickFetch)
            .flatMapSingle(startDate -> historyWrapper.getTicks(instrument,
                                                                startDate,
                                                                startDate + tickFetchMillis - 1));
    }

    public Single<List<IBar>> barsByShiftAdapted(final BarParams barParams,
                                                 final long endTime,
                                                 final int shift) {
        final long periodInterval = barParams
            .period()
            .getInterval();

        return Single
            .defer(() -> adaptBarFetchEndTime(barParams,
                                              periodInterval,
                                              endTime))
            .flatMap(endDate -> historyWrapper.getBars(barParams,
                                                       endDate - shift * periodInterval,
                                                       endDate));
    }

    public List<BarQuote> alignToBarQuotes(final List<IBar> bars,
                                           final BarParams barParams) {
        return reverseQuotes(barsToQuotes(bars, barParams));
    }

    public List<TickQuote> alignToTickQuotes(final List<ITick> ticks,
                                             final Instrument instrument) {
        return reverseQuotes(ticksToQuotes(ticks, instrument));
    }

    public List<BarQuote> barsToQuotes(final List<IBar> bars,
                                       final BarParams barParams) {
        return bars
            .stream()
            .map(bar -> new BarQuote(bar, barParams))
            .collect(Collectors.toList());
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

    public Single<Long> adaptBarFetchEndTime(final BarParams barParams,
                                             final long periodInterval,
                                             final long endTime) {
        return historyWrapper.getBar(barParams, 1)
            .map(IBar::getTime)
            .map(latestBarTime -> endTime > latestBarTime + periodInterval
                    ? latestBarTime
                    : endTime - periodInterval);
    }

    public Single<Long> adaptTickFetchEndTime(final Instrument instrument,
                                              final long endTime) {
        return historyWrapper
            .getTimeOfLastTick(instrument)
            .map(latestTickTime -> endTime > latestTickTime
                    ? latestTickTime
                    : endTime);
    }

    public Observable<Long> countStreamForTickFetch(final long endTime) {
        final LongStream counter = LongStream
            .iterate(1, i -> i + 1)
            .map(count -> endTime - count * tickFetchMillis + 1);
        return Observable.fromIterable(counter::iterator);
    }
}
