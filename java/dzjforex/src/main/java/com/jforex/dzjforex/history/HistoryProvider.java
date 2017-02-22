package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

public class HistoryProvider {

    private final IHistory history;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(HistoryProvider.class);

    public HistoryProvider(final IHistory history,
                           final PluginConfig pluginConfig) {
        this.history = history;
        this.pluginConfig = pluginConfig;
    }

    public List<IBar> fetchBars(final Instrument instrument,
                                final Period period,
                                final OfferSide offerSide,
                                final long startTime,
                                final long endTime) {
        final List<IBar> bars = Observable
            .fromCallable(() -> history.getBars(instrument,
                                                period,
                                                offerSide,
                                                startTime,
                                                endTime))
            .doOnSubscribe(d -> logFetchBarsSubscribe(instrument,
                                                      period,
                                                      offerSide,
                                                      startTime,
                                                      endTime))
            .doOnError(err -> logger.error("Fetching bars  for " + instrument + " failed! " + err.getMessage()))
            .doOnComplete(() -> logger.debug("Fetching bars  for " + instrument + " completed."))
            .retryWhen(this::historyRetryWhen)
            .onErrorResumeNext(Observable.just(new ArrayList<>()))
            .blockingFirst();

        return bars;
    }

    private void logFetchBarsSubscribe(final Instrument instrument,
                                       final Period period,
                                       final OfferSide offerSide,
                                       final long startTime,
                                       final long endTime) {
        logger.debug("Starting to fetch bars for " + instrument + "\n"
                + "period: " + period + "\n"
                + "offerSide: " + offerSide + "\n"
                + "startTime: " + DateTimeUtil.formatMillis(startTime) + "\n"
                + "endTime: " + DateTimeUtil.formatMillis(endTime));
    }

    private ObservableSource<Long> historyRetryWhen(final Observable<Throwable> errors) {
        return errors
            .zipWith(Observable.range(1, pluginConfig.historyDownloadRetries()), (n, i) -> i)
            .flatMap(retryCount -> Observable.timer(pluginConfig.historyRetryDelay(), TimeUnit.MILLISECONDS));
    }

    public List<ITick> fetchTicks(final Instrument instrument,
                                  final long startTime,
                                  final long endTime) {
        return Observable
            .fromCallable(() -> history.getTicks(instrument,
                                                 startTime,
                                                 endTime))
            .doOnSubscribe(d -> logFetchTicksSubscribe(instrument,
                                                       startTime,
                                                       endTime))
            .doOnError(err -> logger.error("Fetching ticks  for " + instrument
                    + " failed! " + err.getMessage()))
            .doOnComplete(() -> logger.debug("Fetching bars  for " + instrument + " completed."))
            .retryWhen(this::historyRetryWhen)
            .onErrorResumeNext(Observable.just(new ArrayList<>()))
            .blockingFirst();
    }

    private void logFetchTicksSubscribe(final Instrument instrument,
                                        final long startTime,
                                        final long endTime) {
        logger.debug("Starting to fetch ticks for " + instrument + "\n"
                + "startTime: " + DateTimeUtil.formatMillis(startTime) + "\n"
                + "endTime: " + DateTimeUtil.formatMillis(endTime));
    }

    public long getBarStart(final Period period,
                            final long barTime) {
        return Observable
            .fromCallable(() -> history.getBarStart(period, barTime))
            .doOnSubscribe(d -> logger.debug("Starting to fetch bar start for \n"
                    + "period: " + period + "\n"
                    + "barTime: " + DateTimeUtil.formatMillis(barTime)))
            .doOnError(err -> logger.error("Fetching bar start failed! " + err.getMessage()))
            .doOnComplete(() -> logger.debug("Fetching bar start completed."))
            .retryWhen(this::historyRetryWhen)
            .onErrorResumeNext(Observable.just(0L))
            .blockingFirst();
    }

    public List<IOrder> ordersByInstrument(final Instrument instrument,
                                           final long from,
                                           final long to) {
        return Observable
            .fromCallable(() -> history.getOrdersHistory(instrument,
                                                         from,
                                                         to))
            .onErrorResumeNext(err -> {
                logger.error("Seeking history orders for " + instrument + " failed! " + err.getMessage());
                return Observable.just(new ArrayList<>());
            })
            .retryWhen(this::historyRetryWhen)
            .blockingFirst();
    }
}
