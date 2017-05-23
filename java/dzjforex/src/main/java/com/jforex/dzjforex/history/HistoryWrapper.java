package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.BarParams;

import io.reactivex.Single;

public class HistoryWrapper {

    private final IHistory history;

    private final static Logger logger = LogManager.getLogger(HistoryWrapper.class);

    public HistoryWrapper(final IHistory history) {
        this.history = history;
    }

    public Single<IBar> getBar(final BarParams barParams,
                               final int shift) {
        final Instrument instrument = barParams.instrument();
        return Single.fromCallable(() -> history.getBar(instrument,
                                                        barParams.period(),
                                                        barParams.offerSide(),
                                                        shift))
            .doOnSubscribe(d -> logger.debug("Fetching bar by " + shift
                    + " shift for " + instrument))
            .doOnError(e -> logger.error("Fetching bar by " + shift
                    + " shift for " + instrument
                    + " failed! " + e.getMessage()))
            .doOnSuccess(bar -> logger.debug("Fetched bar by " + shift
                    + " for " + instrument));
    }

    public Single<List<IBar>> getBarsReversed(final BarParams barParams,
                                              final long startDate,
                                              final long endDate) {
        final Instrument instrument = barParams.instrument();
        return Single
            .fromCallable(() -> history.getBars(instrument,
                                                barParams.period(),
                                                barParams.offerSide(),
                                                startDate,
                                                endDate))
            .map(this::reverseQuotes)
            .doOnSubscribe(d -> logger.debug("Fetching bars for " + instrument + ":\n"
                    + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                    + "endDate: " + DateTimeUtil.formatMillis(endDate)))
            .doOnError(e -> logger.error("Fetching bars for " + instrument
                    + " failed! " + e.getMessage()))
            .doOnSuccess(bars -> logger.debug("Fetched " + bars.size()
                    + " bars for " + instrument));
    }

    public Single<List<ITick>> getTicksReversed(final Instrument instrument,
                                                final long startDate,
                                                final long endDate) {
        return Single.fromCallable(() -> history.getTicks(instrument,
                                                          startDate,
                                                          endDate))
            .map(this::reverseQuotes)
            .doOnSubscribe(d -> logger.debug("Fetching ticks for " + instrument + ":\n"
                    + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                    + "endDate: " + DateTimeUtil.formatMillis(endDate)))
            .doOnError(e -> logger.error("Fetching ticks for " + instrument
                    + " failed! " + e.getMessage()))
            .doOnSuccess(ticks -> logger.debug("Fetched " + ticks.size()
                    + " ticks for " + instrument));
    }

    private <T> List<T> reverseQuotes(final List<T> quotes) {
        Collections.reverse(quotes);
        return quotes;
    }

    public Single<Long> getTimeOfLastTick(final Instrument instrument) {
        return Single.fromCallable(() -> history.getTimeOfLastTick(instrument))
            .doOnSubscribe(d -> logger.debug("Fetching latest tick time for " + instrument))
            .doOnError(e -> logger.error("Fetching latest tick time for " + instrument
                    + " failed! " + e.getMessage()))
            .doOnSuccess(tickTime -> logger.debug("Fetched latest tick time " + DateTimeUtil.formatMillis(tickTime)
                    + " for " + instrument));
    }

    public Single<List<IOrder>> getOrdersHistory(final Instrument instrument,
                                                 final long startDate,
                                                 final long endDate) {
        return Single.fromCallable(() -> history.getOrdersHistory(instrument,
                                                                  startDate,
                                                                  endDate))
            .doOnSubscribe(d -> logger.debug("Fetching orders from history for " + instrument + ":\n"
                    + "startDate: " + DateTimeUtil.formatMillis(startDate) + "\n"
                    + "endDate: " + DateTimeUtil.formatMillis(endDate)))
            .doOnError(e -> logger.error("Fetching orders from history for " + instrument
                    + " failed! " + e.getMessage()))
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size()
                    + " orders from history for " + instrument));
    }
}
