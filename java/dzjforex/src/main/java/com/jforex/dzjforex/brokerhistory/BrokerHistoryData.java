package com.jforex.dzjforex.brokerhistory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import com.jforex.dzjforex.brokertime.TimeConvert;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

public class BrokerHistoryData {

    private final String instrumentName;
    private final double startTime;
    private final double endTime;
    private final int noOfTickMinutes;
    private final int noOfRequestedTicks;
    private final HistoryTickFiller historyTickFiller;
    private final static int zorroTickSize = 7;

    public BrokerHistoryData(final String instrumentName,
                             final double startTime,
                             final double endTime,
                             final int noOfTickMinutes,
                             final int noOfRequestedTicks,
                             final HistoryTickFiller historyTickFiller) {
        this.instrumentName = instrumentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.noOfTickMinutes = noOfTickMinutes;
        this.noOfRequestedTicks = noOfRequestedTicks;
        this.historyTickFiller = historyTickFiller;
    }

    public String instrumentName() {
        return instrumentName;
    }

    public long startTimeForBar() {
        return TimeConvert.millisFromOLEDateRoundMinutes(startTime);
    }

    public long endTimeForBar() {
        return TimeConvert.millisFromOLEDateRoundMinutes(endTime);
    }

    public long startTimeForTick() {
        return TimeConvert.millisFromOLEDate(startTime);
    }

    public long endTimeForTick() {
        return TimeConvert.millisFromOLEDate(endTime) - 2;
    }

    public int noOfTickMinutes() {
        return noOfTickMinutes;
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
