package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
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
                                final Filter filter,
                                final long startTime,
                                final long endTime) {
        return Observable
            .fromCallable(() -> history.getBars(instrument,
                                                period,
                                                offerSide,
                                                filter,
                                                startTime,
                                                endTime))
            .doOnSubscribe(d -> logFetchBarsSubscribe(instrument,
                                                      period,
                                                      offerSide,
                                                      filter,
                                                      startTime,
                                                      endTime))
            .doOnError(err -> logger.error("Fetching bars  for " + instrument + " failed! " + err.getMessage()))
            .doOnComplete(() -> logger.debug("Fetching bars  for " + instrument + " completed."))
            .retryWhen(this::historyRetryWhen)
            .onErrorResumeNext(Observable.just(new ArrayList<>()))
            .blockingFirst();
    }

    private void logFetchBarsSubscribe(final Instrument instrument,
                                       final Period period,
                                       final OfferSide offerSide,
                                       final Filter filter,
                                       final long startTime,
                                       final long endTime) {
        logger.debug("Starting to fetch bars for " + instrument + "\n"
                + "period: " + period + "\n"
                + "offerSide: " + offerSide + "\n"
                + "filter: " + filter + "\n"
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

    public long getPreviousBarStart(final Period period,
                                    final long barTime) {
        return Observable
            .fromCallable(() -> history.getPreviousBarStart(period, barTime))
            .doOnSubscribe(d -> logger.debug("Starting to fetch previous bar start for \n"
                    + "period: " + period + "\n"
                    + "barTime: " + DateTimeUtil.formatMillis(barTime)))
            .doOnError(err -> logger.error("Fetching previous bar start failed! " + err.getMessage()))
            .doOnComplete(() -> logger.debug("Fetching previous bar start completed."))
            .retryWhen(this::historyRetryWhen)
            .onErrorResumeNext(Observable.just(0L))
            .blockingFirst();
    }

    public IOrder orderByID(final int orderID) {
        return Observable
            .fromCallable(() -> history.getHistoricalOrderById(String.valueOf(orderID)))
            .doOnSubscribe(d -> logger.debug("Seeking orderID " + orderID + " in history..."))
            .onErrorResumeNext(err -> {
                logger.error("Seeking orderID " + orderID + " in history failed! " + err.getMessage());
                return Observable.just(null);
            })
            .doOnNext(order -> {
                if (order == null)
                    logger.error("Found no order for orderID " + orderID + " in history!");
            })
            .doOnComplete(() -> logger.debug("Found order ID " + orderID + " in history."))
            .blockingFirst();
    }

    public List<IOrder> ordersByInstrument(final Instrument instrument,
                                           final long from,
                                           final long to) {
        return Observable
            .fromCallable(() -> history.getOrdersHistory(instrument,
                                                         from,
                                                         to))
            .doOnSubscribe(d -> logger.debug("Seeking orders for " + instrument
                    + " from " + DateTimeUtil.formatMillis(from)
                    + " from " + DateTimeUtil.formatMillis(to)))
            .onErrorResumeNext(err -> {
                logger.error("Seeking orders for " + instrument + " in history failed! " + err.getMessage());
                return Observable.just(new ArrayList<>());
            })
            .retryWhen(this::historyRetryWhen)
            .blockingFirst();
    }
}
