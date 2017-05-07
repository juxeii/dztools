package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

import io.reactivex.Observable;
import io.reactivex.Single;

public class BarFetcher {

    private final HistoryProvider historyProvider;
    private final Zorro zorro;

    public BarFetcher(final HistoryProvider historyProvider,
                      final Zorro zorro) {
        this.historyProvider = historyProvider;
        this.zorro = zorro;
    }

    public int run(final Instrument instrument,
                   final BrokerHistoryData brokerHistoryData) {
        final long startDate = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.startTime());
        final long endDate = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.endTime());
        final BarParams barParams = createParams(instrument, brokerHistoryData);

        final Observable<Integer> fetchResult = historyProvider
            .barsByShift(barParams,
                         endDate,
                         brokerHistoryData.noOfRequestedTicks() - 1)
            .flattenAsObservable(bars -> bars)
            .filter(barQuote -> isQuoteAfterStartDate(barQuote, startDate))
            .toList()
            .doOnSuccess(brokerHistoryData::fillBarQuotes)
            .map(List::size)
            .onErrorResumeNext(Single.just(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()))
            .toObservable();

        return zorro.progressWait(fetchResult);
    }

    private BarParams createParams(final Instrument instrument,
                                   final BrokerHistoryData brokerHistoryData) {
        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        return BarParams
            .forInstrument(instrument)
            .period(period)
            .offerSide(OfferSide.ASK);
    }

    private boolean isQuoteAfterStartDate(final BarQuote barQuote,
                                          final long startDate) {
        return barQuote
            .bar()
            .getTime() >= startDate;
    }
}
