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
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

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
                                             final long startDate,
                                             final long endDate) {
        final Instrument instrument = barParams.instrument();
        return Single
            .fromCallable(() -> history.getBars(barParams.instrument(),
                                                barParams.period(),
                                                barParams.offerSide(),
                                                startDate,
                                                endDate))
            .doOnSubscribe(d -> logger.debug("Fetching bars for " + instrument + ":\n"
                    + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                    + "endDate: " + DateTimeUtil.formatMillis(endDate) + "\n"))
            .flattenAsObservable(bars -> bars)
            .map(bar -> new BarQuote(bar, barParams))
            .toList()
            .map(this::reverseQuotes)
            .doOnError(e -> logger.error("Fetching bars for " + instrument + " failed! " + e.getMessage()))
            .doOnSuccess(bars -> logger.debug("Fetched " + bars.size() + " bars for " + instrument));
    }

    private Single<IBar> barByShift(final BarParams barParams,
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
        final long periodInterval = barParams
            .period()
            .getInterval();

        return Single
            .defer(() -> adaptBarFetchEndTime(barParams,
                                              periodInterval,
                                              endTime))
            .flatMap(endDate -> {
                final long startDate = endDate - shift * periodInterval;
                return fetchBars(barParams,
                                 startDate,
                                 endDate);
            })
            .flattenAsObservable(bars -> bars)
            .toList()
            .retryWhen(RxUtility.retryForHistory(pluginConfig));
    }

    private Single<Long> adaptBarFetchEndTime(final BarParams barParams,
                                              final long periodInterval,
                                              final long endTime) {
        final long latestBarTime = barByShift(barParams, 1)
            .blockingGet()
            .getTime();
        return Single.just(endTime > latestBarTime + periodInterval
                ? latestBarTime
                : endTime - periodInterval);
    }

    private Single<List<TickQuote>> fetchTicks(final Instrument instrument,
                                               final long startDate,
                                               final long endDate) {
        return Single.fromCallable(() -> history.getTicks(instrument,
                                                          startDate,
                                                          endDate))
            .doOnSubscribe(d -> logger.debug("Fetching ticks for " + instrument + ":\n"
                    + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                    + "endDate: " + DateTimeUtil.formatMillis(endDate) + "\n"))
            .flattenAsObservable(ticks -> ticks)
            .map(tick -> new TickQuote(instrument, tick))
            .toList()
            .map(this::reverseQuotes)
            .doOnError(e -> logger.error("Fetching ticks for " + instrument + " failed! " + e.getMessage()))
            .doOnSuccess(ticks -> logger.debug("Fetched " + ticks.size() + " ticks for " + instrument));
    }

    public Single<List<TickQuote>> ticksByShift(final Instrument instrument,
                                                final long endDate,
                                                final int shift) {
        final int requestedTicks = shift + 1;
        return Single
            .defer(() -> adaptTickFetchEndTime(instrument, endDate))
            .flatMapObservable(this::countStreamForTickFetch)
            .flatMapSingle(startTime -> {
                final long endDateAdapted = startTime + tickFetchMillis - 1;
                return fetchTicks(instrument,
                                  startTime,
                                  endDateAdapted);
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
            .retryWhen(RxUtility.retryForHistory(pluginConfig));
    }

    private Single<Long> adaptTickFetchEndTime(final Instrument instrument,
                                               final long endTime) {
        return latestTickTime(instrument).map(latestTickTime -> endTime > latestTickTime
                ? latestTickTime
                : endTime);
    }

    private Observable<Long> countStreamForTickFetch(final long endTime) {
        final LongStream counter = LongStream
            .iterate(1, i -> i + 1)
            .map(count -> endTime - count * tickFetchMillis + 1);
        return Observable.fromIterable(counter::iterator);
    }

    private Single<Long> latestTickTime(final Instrument instrument) {
        return Single.fromCallable(() -> history.getTimeOfLastTick(instrument));
    }

    private <T> List<T> reverseQuotes(final List<T> quotes) {
        Collections.reverse(quotes);
        return quotes;
    }

    public Single<List<IOrder>> ordersByInstrument(final Instrument instrument,
                                                   final long startTime,
                                                   final long endTime) {
        return Single.fromCallable(() -> history.getOrdersHistory(instrument,
                                                                  startTime,
                                                                  endTime));
    }
}
