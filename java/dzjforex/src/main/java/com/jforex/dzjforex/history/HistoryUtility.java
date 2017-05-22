package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.quote.BarParams;

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
                                                                startDate + tickFetchMillis - 1))
            .map(this::reverseQuotes);
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
                                                       endDate))
            .map(this::reverseQuotes);
    }

    private <T> List<T> reverseQuotes(final List<T> quotes) {
        Collections.reverse(quotes);
        return quotes;
    }

    private Single<Long> adaptBarFetchEndTime(final BarParams barParams,
                                              final long periodInterval,
                                              final long endTime) {
        return historyWrapper
            .getBar(barParams, 1)
            .map(IBar::getTime)
            .map(latestBarTime -> endTime > latestBarTime + periodInterval
                    ? latestBarTime
                    : endTime - periodInterval);
    }

    private Single<Long> adaptTickFetchEndTime(final Instrument instrument,
                                               final long endTime) {
        return historyWrapper
            .getTimeOfLastTick(instrument)
            .map(latestTickTime -> endTime > latestTickTime
                    ? latestTickTime
                    : endTime);
    }

    private Observable<Long> countStreamForTickFetch(final long endTime) {
        final LongStream counter = LongStream
            .iterate(1, i -> i + 1)
            .map(count -> endTime - count * tickFetchMillis + 1);
        return Observable.fromIterable(counter::iterator);
    }
}
