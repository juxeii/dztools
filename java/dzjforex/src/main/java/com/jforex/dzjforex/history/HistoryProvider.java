package com.jforex.dzjforex.history;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.rx.RetryWhenFunctionForSingle;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryProvider {

    private final IHistory history;
    private final PluginConfig pluginConfig;
    private final long tickFetchMillis;

    public HistoryProvider(final IHistory history,
                           final PluginConfig pluginConfig) {
        this.history = history;
        this.pluginConfig = pluginConfig;

        tickFetchMillis = TimeUnit.MILLISECONDS.convert(pluginConfig.tickFetchMinutes(), TimeUnit.MINUTES);
    }

    public Single<List<IBar>> fetchBars(final BarParams barParams,
                                        final long startTime,
                                        final long endTime) {
        return Single.fromCallable(() -> history.getBars(barParams.instrument(),
                                                         barParams.period(),
                                                         barParams.offerSide(),
                                                         startTime,
                                                         endTime));
    }

    public Single<IBar> barByShift(final BarParams barParams,
                                   final int shift) {
        return Single.fromCallable(() -> history.getBar(barParams.instrument(),
                                                        barParams.period(),
                                                        barParams.offerSide(),
                                                        shift));
    }

    public Single<List<ITick>> ticksByShift(final Instrument instrument,
                                            final long endTime,
                                            final int shift) {
        final int requestedTicks = shift + 1;

        return Observable
            .range(1, Integer.MAX_VALUE)
            .flatMapSingle(count -> {
                final long fetchStart = endTime - count * tickFetchMillis + 1;
                final long fetchEnd = fetchStart + tickFetchMillis - 1;
                return fetchTicks(instrument,
                                  fetchStart,
                                  fetchEnd);
            })
            .map(ticks -> {
                final int noOfTicks = ticks.size();
                return noOfTicks <= requestedTicks
                        ? ticks
                        : ticks.subList(noOfTicks - shift - 1, noOfTicks);
            })
            .flatMapIterable(ticks -> ticks)
            .take(requestedTicks)
            .toList();
    }

    public Single<Long> latestBarTime(final BarParams barParams) {
        return barByShift(barParams, 1).map(IBar::getTime);
    }

    public Single<Long> previousBarStart(final Period period,
                                         final long time) {
        return Single.fromCallable(() -> history.getPreviousBarStart(period, time));
    }

    public Single<List<ITick>> fetchTicks(final Instrument instrument,
                                          final long startTime,
                                          final long endTime) {
        return Single.fromCallable(() -> history.getTicks(instrument,
                                                          startTime,
                                                          endTime));
    }

    public Single<ITick> tickByShift(final Instrument instrument,
                                     final int shift) {
        return Single.fromCallable(() -> history.getTick(instrument, shift));
    }

    public Single<List<IOrder>> ordersByInstrument(final Instrument instrument,
                                                   final long startTime,
                                                   final long endTime) {
        return Single.fromCallable(() -> history.getOrdersHistory(instrument,
                                                                  startTime,
                                                                  endTime));
    }

    public RetryParams retryParams(final int retries,
                                   final long delay) {
        final RetryDelay retryDelay = new RetryDelay(delay, TimeUnit.MILLISECONDS);
        return new RetryParams(retries, att -> retryDelay);
    }

    public RetryParams retryParamsForHistory() {
        return retryParams(pluginConfig.historyAccessRetries(), pluginConfig.historyAccessRetryDelay());
    }

    public RetryWhenFunctionForSingle retryForHistory() {
        return RxUtil.retryWithDelayForSingle(retryParamsForHistory());
    }
}
