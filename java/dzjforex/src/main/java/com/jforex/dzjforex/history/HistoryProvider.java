package com.jforex.dzjforex.history;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryProvider {

    private final HistoryUtility historyUtility;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(HistoryProvider.class);

    public HistoryProvider(final HistoryUtility historyUtility,
                           final PluginConfig pluginConfig) {
        this.historyUtility = historyUtility;
        this.pluginConfig = pluginConfig;
    }

    public Single<List<BarQuote>> barsByShift(final BarParams barParams,
                                              final long endTime,
                                              final int shift) {
        return Single
            .defer(() -> historyUtility.barsByShiftAdapted(barParams,
                                                           endTime,
                                                           shift))
            .map(bars -> historyUtility.alignToBarQuotes(bars, barParams))
            .retryWhen(RxUtility.retryForHistory(pluginConfig))
            .doOnSuccess(barQuotes -> logger.debug("Fetched " + barQuotes.size()
                    + " bar quotes for " + barParams.instrument()));
    }

    public Single<List<TickQuote>> ticksByShift(final Instrument instrument,
                                                final long endDate,
                                                final int shift) {
        return Observable
            .defer(() -> historyUtility.ticksByShiftAdapted(instrument,
                                                            endDate,
                                                            shift))
            .map(ticks -> historyUtility.alignToTickQuotes(ticks, instrument))
            .flatMapIterable(ticks -> ticks)
            .take(shift + 1)
            .toList()
            .retryWhen(RxUtility.retryForHistory(pluginConfig))
            .doOnSuccess(tickQuotes -> logger.debug("Fetched " + tickQuotes.size()
                    + " tick quotes for " + instrument));
    }
}
