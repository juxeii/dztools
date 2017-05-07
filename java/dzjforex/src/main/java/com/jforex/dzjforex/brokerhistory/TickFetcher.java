package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.quote.TickQuote;

import io.reactivex.Observable;
import io.reactivex.Single;

public class TickFetcher {

    private final HistoryProvider historyProvider;
    private final Zorro zorro;

    public TickFetcher(final HistoryProvider historyProvider,
                       final Zorro zorro) {
        this.historyProvider = historyProvider;
        this.zorro = zorro;
    }

    public int run(final Instrument instrument,
                   final BrokerHistoryData brokerHistoryData) {
        final long startDate = TimeConvert.millisFromOLEDate(brokerHistoryData.startTime());
        final long endDate = TimeConvert.millisFromOLEDate(brokerHistoryData.endTime()) - 2;

        final Observable<Integer> fetchResult = historyProvider
            .ticksByShift(instrument,
                          endDate,
                          brokerHistoryData.noOfRequestedTicks() - 1)
            .flattenAsObservable(ticks -> ticks)
            .filter(tickQuote -> isQuoteAfterStartDate(tickQuote, startDate))
            .toList()
            .doOnSuccess(brokerHistoryData::fillTickQuotes)
            .map(List::size)
            .onErrorResumeNext(Single.just(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()))
            .toObservable();

        return zorro.progressWait(fetchResult);
    }

    private boolean isQuoteAfterStartDate(final TickQuote tickQuote,
                                          final long startDate) {
        return tickQuote
            .tick()
            .getTime() >= startDate;
    }
}
