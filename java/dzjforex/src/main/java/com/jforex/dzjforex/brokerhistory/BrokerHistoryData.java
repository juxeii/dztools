package com.jforex.dzjforex.brokerhistory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import com.jforex.dzjforex.brokertime.TimeConvert;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

public class BrokerHistoryData {

    private final String assetName;
    private final double utcStartDate;
    private final double utcEndDate;
    private final int periodInMinutes;
    private final int noOfRequestedTicks;
    private final HistoryTickFiller historyTickFiller;
    private final static int zorroTickSize = 7;

    public BrokerHistoryData(final String assetName,
                             final double utcStartDate,
                             final double utcEndDate,
                             final int periodInMinutes,
                             final int noOfTicks,
                             final HistoryTickFiller historyTickFiller) {
        this.assetName = assetName;
        this.utcStartDate = utcStartDate;
        this.utcEndDate = utcEndDate;
        this.periodInMinutes = periodInMinutes;
        this.noOfRequestedTicks = noOfTicks;
        this.historyTickFiller = historyTickFiller;
    }

    public String assetName() {
        return assetName;
    }

    public long startTimeForBar() {
        return TimeConvert.millisFromOLEDateRoundMinutes(utcStartDate);
    }

    public long endTimeForBar() {
        return TimeConvert.millisFromOLEDateRoundMinutes(utcEndDate);
    }

    public long startTimeForTick() {
        return TimeConvert.millisFromOLEDate(utcStartDate);
    }

    public long endTimeForTick() {
        return TimeConvert.millisFromOLEDate(utcEndDate) - 2;
    }

    public int periodInMinutes() {
        return periodInMinutes;
    }

    public int noOfRequestedTicks() {
        return noOfRequestedTicks;
    }

    public void fillBarQuotes(final List<BarQuote> barQuotes) {
        genericFill(barQuotes, historyTickFiller::fillBarQuote);
    }

    public void fillTickQuotes(final List<TickQuote> tickQuotes) {
        genericFill(tickQuotes, historyTickFiller::fillTickQuote);
    }

    private <T> void genericFill(final List<T> quotes,
                                 final BiConsumer<T, Integer> filler) {
        IntStream
            .range(0, quotes.size())
            .forEach(index -> {
                final T quote = quotes.get(index);
                filler.accept(quote, index * zorroTickSize);
            });
    }
}
