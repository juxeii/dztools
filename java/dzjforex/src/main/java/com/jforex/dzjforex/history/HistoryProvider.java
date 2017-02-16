package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import io.reactivex.Observable;

public class HistoryProvider {

    private final IHistory history;

    private final static Logger logger = LogManager.getLogger(BrokerHistory2.class);

    public HistoryProvider(final IHistory history) {
        this.history = history;
    }

    public List<IBar> fetchBars(final Instrument instrument,
                                final Period period,
                                final OfferSide offerSide,
                                final Filter filter,
                                final long startTime,
                                final long endTime) {
        return Observable
            .fromCallable(() -> {
                history.getBar(instrument,
                               period,
                               offerSide,
                               0);
                final long prevBarStart = getPreviousBarStart(period, history.getLastTick(instrument).getTime());
                final long adjustedEndTime = prevBarStart < endTime ? prevBarStart : endTime;

                return history.getBars(instrument,
                                       period,
                                       offerSide,
                                       filter,
                                       startTime,
                                       adjustedEndTime);
            })
            .doOnError(err -> logger.error("fetchBars exception: " + err.getMessage()))
            .onErrorResumeNext(Observable.just(new ArrayList<>()))
            .blockingFirst();
    }

    public long getPreviousBarStart(final Period period,
                                    final long barTime) {
        long previousBarTime = 0L;
        try {
            previousBarTime = history.getPreviousBarStart(period, barTime);
        } catch (final JFException e) {
            logger.error("getPreviousBarStart exc: " + e.getMessage());
        }
        return previousBarTime;
    }
}
