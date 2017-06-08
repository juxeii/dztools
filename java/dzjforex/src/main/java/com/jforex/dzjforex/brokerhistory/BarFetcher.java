package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.misc.TimeConvert;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

import io.reactivex.Single;

public class BarFetcher {

    private final BarHistoryByShift barHistoryByShift;

    public BarFetcher(final BarHistoryByShift barHistoryByShift) {
        this.barHistoryByShift = barHistoryByShift;
    }

    public Single<Integer> run(final Instrument instrument,
                               final BrokerHistoryData brokerHistoryData) {
        final BarParams barParams = createParams(instrument, brokerHistoryData);
        return Single
            .defer(() -> barHistoryByShift.get(barParams,
                                               brokerHistoryData.endTimeForBar(),
                                               brokerHistoryData.noOfRequestedTicks() - 1))
            .flattenAsObservable(bars -> bars)
            .filter(bar -> bar.getTime() >= brokerHistoryData.startTimeForBar())
            .map(bar -> new BarQuote(bar, barParams))
            .toList()
            .doOnSuccess(brokerHistoryData::fillBarQuotes)
            .map(List::size);
    }

    private BarParams createParams(final Instrument instrument,
                                   final BrokerHistoryData brokerHistoryData) {
        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.periodInMinutes());
        return BarParams
            .forInstrument(instrument)
            .period(period)
            .offerSide(OfferSide.ASK);
    }
}
