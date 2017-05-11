package com.jforex.dzjforex.brokerhistory;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

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
        final BarParams barParams = createParams(instrument, brokerHistoryData);
        final Single<Integer> fetchResult = historyProvider
            .barsByShift(barParams,
                         brokerHistoryData.endTimeForBar(),
                         brokerHistoryData.noOfRequestedTicks() - 1)
            .map(barQuotes -> filterTime(barQuotes, brokerHistoryData.startTimeForBar()))
            .doOnSuccess(brokerHistoryData::fillBarQuotes)
            .map(List::size)
            .onErrorReturnItem(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue());

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

    public List<BarQuote> filterTime(final List<BarQuote> barQuotes,
                                     final long startDate) {
        return barQuotes
            .stream()
            .filter(barQuote -> barQuote
                .bar()
                .getTime() >= startDate)
            .collect(Collectors.toList());
    }
}
