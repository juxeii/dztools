package com.jforex.dzjforex.brokerhistory;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

import io.reactivex.Single;

public class BarFetcher {

    private final HistoryProvider historyProvider;

    public BarFetcher(final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    public Single<Integer> run(final Instrument instrument,
                               final BrokerHistoryData brokerHistoryData) {
        final BarParams barParams = createParams(instrument, brokerHistoryData);
        return historyProvider
            .barsByShift(barParams,
                         brokerHistoryData.endTimeForBar(),
                         brokerHistoryData.noOfRequestedTicks() - 1)
            .map(bars -> filterTime(bars, brokerHistoryData.startTimeForBar()))
            .map(bars -> barsToQuotes(bars, barParams))
            .doOnSuccess(brokerHistoryData::fillBarQuotes)
            .map(List::size);
    }

    private BarParams createParams(final Instrument instrument,
                                   final BrokerHistoryData brokerHistoryData) {
        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        return BarParams
            .forInstrument(instrument)
            .period(period)
            .offerSide(OfferSide.ASK);
    }

    private List<IBar> filterTime(final List<IBar> bars,
                                  final long startDate) {
        return bars
            .stream()
            .filter(bar -> bar.getTime() >= startDate)
            .collect(Collectors.toList());
    }

    private List<BarQuote> barsToQuotes(final List<IBar> bars,
                                        final BarParams barParams) {
        return bars
            .stream()
            .map(bar -> new BarQuote(bar, barParams))
            .collect(Collectors.toList());
    }
}
