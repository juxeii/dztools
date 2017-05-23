package com.jforex.dzjforex.history;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.quote.BarParams;

import io.reactivex.Single;

public class BarHistoryByShift {

    private final HistoryWrapper historyWrapper;
    private final HistoryFetchDate historyFetchDate;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(BarHistoryByShift.class);

    public BarHistoryByShift(final HistoryWrapper historyWrapper,
                             final HistoryFetchDate historyFetchDate,
                             final PluginConfig pluginConfig) {
        this.historyWrapper = historyWrapper;
        this.historyFetchDate = historyFetchDate;
        this.pluginConfig = pluginConfig;
    }

    public Single<List<IBar>> get(final BarParams barParams,
                                  final long endDate,
                                  final int shift) {
        return historyFetchDate
            .endDateForBar(barParams, endDate)
            .flatMap(endDateAdapted -> getBarsReversed(barParams,
                                                       endDateAdapted,
                                                       shift))
            .retryWhen(RxUtility.retryForHistory(pluginConfig))
            .doOnSuccess(bars -> logger.debug("Fetched " + bars.size() + " bars for " + barParams.instrument()));
    }

    private Single<List<IBar>> getBarsReversed(final BarParams barParams,
                                               final long endDate,
                                               final int shift) {
        final long periodInterval = barParams
            .period()
            .getInterval();

        return historyWrapper.getBarsReversed(barParams,
                                              endDate - shift * periodInterval,
                                              endDate);
    }
}
