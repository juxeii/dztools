package com.jforex.dzjforex.history;

import java.util.List;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarParams;

import io.reactivex.Single;

public class HistoryProvider {

    private final IHistory history;

    public HistoryProvider(final IHistory history) {
        this.history = history;
    }

    public Single<List<IBar>> fetchBars(final BarParams barParams,
                                        final long startTime,
                                        final long endTime) {
        return Single.fromCallable(() -> history.getBars(barParams.instrument(),
                                                         barParams.period(),
                                                         barParams.offerSide(),
                                                         startTime,
                                                         endTime));
    }

    public Single<IBar> barByShift(final BarParams barParams,
                                   final int shift) {
        return Single.fromCallable(() -> history.getBar(barParams.instrument(),
                                                        barParams.period(),
                                                        barParams.offerSide(),
                                                        shift));
    }

    public Single<Long> latestBarTime(final BarParams barParams) {
        return barByShift(barParams, 1).map(IBar::getTime);
    }

    public Single<Long> previousBarStart(final Period period,
                                         final long time) {
        return Single.fromCallable(() -> history.getPreviousBarStart(period, time));
    }

    public Single<List<ITick>> fetchTicks(final Instrument instrument,
                                          final long startTime,
                                          final long endTime) {
        return Single.fromCallable(() -> history.getTicks(instrument,
                                                          startTime,
                                                          endTime));
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
}
