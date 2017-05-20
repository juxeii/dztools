package com.jforex.dzjforex.history;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.quote.BarParams;

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

    public Single<List<IBar>> barsByShift(final BarParams barParams,
                                          final long endTime,
                                          final int shift) {
        return Single
            .defer(() -> historyUtility.barsByShiftAdapted(barParams,
                                                           endTime,
                                                           shift))
            .retryWhen(RxUtility.retryForHistory(pluginConfig))
            .doOnSuccess(bars -> logger.debug("Fetched " + bars.size()
                    + " bars for " + barParams.instrument()));
    }

    public Single<List<ITick>> ticksByShift(final Instrument instrument,
                                            final long endDate,
                                            final int shift) {
        return Observable
            .defer(() -> historyUtility.ticksByShiftAdapted(instrument,
                                                            endDate,
                                                            shift))
            .flatMapIterable(ticks -> ticks)
            .take(shift + 1)
            .toList()
            .retryWhen(RxUtility.retryForHistory(pluginConfig))
            .doOnSuccess(ticks -> logger.debug("Fetched " + ticks.size()
                    + " ticks for " + instrument));
    }
}
