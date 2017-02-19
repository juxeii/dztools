package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;

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
            .retry(pluginConfig.historyDownloadRetries())
            .doOnError(err -> logger.error("Fetching bars  for " + instrument + " failed! " + err.getMessage()))
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
            .retry(pluginConfig.historyDownloadRetries())
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
}
