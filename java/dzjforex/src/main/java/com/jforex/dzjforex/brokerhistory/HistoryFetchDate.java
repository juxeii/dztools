package com.jforex.dzjforex.brokerhistory;

import java.util.stream.LongStream;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.programming.quote.BarParams;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryFetchDate {

    private final HistoryWrapper historyWrapper;
    private final long tickFetchMillis;

    public HistoryFetchDate(final HistoryWrapper historyWrapper,
                            final PluginConfig pluginConfig) {
        this.historyWrapper = historyWrapper;

        tickFetchMillis = pluginConfig.tickFetchMillis();
    }

    public Single<Long> endDateForBar(final BarParams barParams,
                                      final long endDate) {
        return historyWrapper
            .getBar(barParams, 1)
            .map(IBar::getTime)
            .map(latestBarTime -> {
                final long periodInterval = barParams
                    .period()
                    .getInterval();
                return endDate > latestBarTime + periodInterval
                        ? latestBarTime
                        : endDate - periodInterval;
            });
    }

    public Observable<Long> startDatesForTick(final Instrument instrument,
                                              final long endDate) {
        return historyWrapper
            .getTimeOfLastTick(instrument)
            .map(latestTickTime -> endDate > latestTickTime
                    ? latestTickTime
                    : endDate)
            .flatMapObservable(this::countStreamForTickFetch);
    }

    private Observable<Long> countStreamForTickFetch(final long endTime) {
        final LongStream counter = LongStream
            .iterate(1, i -> i + 1)
            .map(count -> endTime - count * tickFetchMillis + 1);
        return Observable.fromIterable(counter::iterator);
    }
}
