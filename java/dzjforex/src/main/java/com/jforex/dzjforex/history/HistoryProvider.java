package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.rx.RetryWhenFunctionForSingle;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryProvider {

    private final IHistory history;
    private final PluginConfig pluginConfig;
    private final long tickFetchMillis;

    private final static Logger logger = LogManager.getLogger(HistoryProvider.class);

    public HistoryProvider(final IHistory history,
                           final PluginConfig pluginConfig) {
        this.history = history;
        this.pluginConfig = pluginConfig;

        tickFetchMillis = TimeUnit.MILLISECONDS.convert(pluginConfig.tickFetchMinutes(), TimeUnit.MINUTES);
    }

    private Single<List<BarQuote>> fetchBars(final BarParams barParams,
                                             final long startTime,
                                             final long endTime) {
        return Single
            .fromCallable(() -> history.getBars(barParams.instrument(),
                                                barParams.period(),
                                                barParams.offerSide(),
                                                startTime,
                                                endTime))
            .flattenAsObservable(bars -> bars)
            .map(bar -> new BarQuote(bar, barParams))
            .toList()
            .map(this::reverseQuotes);
    }

    public Single<IBar> barByShift(final BarParams barParams,
                                   final int shift) {
        return Single.fromCallable(() -> history.getBar(barParams.instrument(),
                                                        barParams.period(),
                                                        barParams.offerSide(),
                                                        shift));
    }

    public Single<List<BarQuote>> barsByShift(final BarParams barParams,
                                              final long endTime,
                                              final int shift) {
        final int requestedBars = shift + 1;
        final Instrument instrument = barParams.instrument();
        final long periodInterval = barParams
            .period()
            .getInterval();

        return Observable
            .defer(() -> {
                final long latestBarTime = barByShift(barParams, 1)
                    .blockingGet()
                    .getTime();
                return Observable.just(endTime > latestBarTime + periodInterval
                        ? latestBarTime
                        : endTime - periodInterval);
            })
            .flatMapSingle(endDate -> {
                final long startDate = endDate - shift * periodInterval;
                logger.debug("Fetching " + requestedBars + " for instrument " + instrument + ":\n"
                        + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                        + "endDate: " + DateTimeUtil.formatMillis(endDate) + "\n");

                return fetchBars(barParams,
                                 startDate,
                                 endDate);
            })
            .flatMapIterable(bars -> bars)
            .toList()
            .doOnSuccess(bars -> logger.debug("Fetched " + bars.size() + " bars for " + instrument))
            .doOnError(err -> logger.error("Fetching bars for " + instrument + " failed! " + err.getMessage()))
            .retryWhen(retry());
    }

    private Single<List<TickQuote>> fetchTicks(final Instrument instrument,
                                               final long startTime,
                                               final long endTime) {
        return Single.fromCallable(() -> history.getTicks(instrument,
                                                          startTime,
                                                          endTime))
            .flattenAsObservable(ticks -> ticks)
            .map(tick -> new TickQuote(instrument, tick))
            .toList()
            .map(this::reverseQuotes);
    }

    public Single<List<TickQuote>> ticksByShift(final Instrument instrument,
                                                final long endTime,
                                                final int shift) {
        final int requestedTicks = shift + 1;
        return Observable
            .defer(() -> {
                final long latestTickTime = latestTickTime(instrument).blockingGet();
                return Observable.just(endTime > latestTickTime
                        ? latestTickTime
                        : endTime);
            })
            .flatMap(adaptedEndTime -> {
                final LongStream counter = LongStream
                    .iterate(1, i -> i + 1)
                    .map(count -> adaptedEndTime - count * tickFetchMillis + 1);
                return Observable.fromIterable(counter::iterator);
            })
            .flatMapSingle(startDate -> {
                final long endDate = startDate + tickFetchMillis - 1;
                logger.debug("Fetching " + requestedTicks + " for instrument " + instrument + ":\n"
                        + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                        + "endDate: " + DateTimeUtil.formatMillis(endDate) + "\n");

                return fetchTicks(instrument,
                                  startDate,
                                  endDate);
            })
            .map(ticks -> {
                final int noOfTicks = ticks.size();
                return noOfTicks <= requestedTicks
                        ? ticks
                        : ticks.subList(noOfTicks - shift - 1, noOfTicks);
            })
            .flatMapIterable(ticks -> ticks)
            .take(requestedTicks)
            .toList()
            .doOnSuccess(ticks -> logger.debug("Fetched " + ticks.size() + " ticks for " + instrument))
            .doOnError(err -> logger.error("Fetching ticks for " + instrument + " failed! " + err.getMessage()))
            .retryWhen(retry());
    }

    public Single<Long> latestTickTime(final Instrument instrument) {
        return Single.fromCallable(() -> history.getTimeOfLastTick(instrument));
    }

    public Single<Long> latestBarTime(final BarParams barParams) {
        return barByShift(barParams, 1).map(IBar::getTime);
    }

    public Single<Long> previousBarStart(final Period period,
                                         final long time) {
        return Single.fromCallable(() -> history.getPreviousBarStart(period, time));
    }

    private <T> List<T> reverseQuotes(final List<T> quotes) {
        Collections.reverse(quotes);
        return quotes;
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

    public RetryWhenFunctionForSingle retry() {
        return RxUtil.retryWithDelayForSingle(retryParamsForHistory());
    }
}
