package com.jforex.dzjforex.brokerhistory;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

public class BrokerHistoryData {

    private final String instrumentName;
    private final double startTime;
    private final double endTime;
    private final int noOfTickMinutes;
    private final int noOfRequestedTicks;
    private final double tickParams[];

    private final static Logger logger = LogManager.getLogger(BrokerHistoryData.class);

    public BrokerHistoryData(final String instrumentName,
                             final double startTime,
                             final double endTime,
                             final int noOfTickMinutes,
                             final int noOfRequestedTicks,
                             final double tickParams[]) {
        this.instrumentName = instrumentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.noOfTickMinutes = noOfTickMinutes;
        this.noOfRequestedTicks = noOfRequestedTicks;
        this.tickParams = tickParams;
    }

    public String instrumentName() {
        return instrumentName;
    }

    public double startTime() {
        return startTime;
    }

    public double endTime() {
        return endTime;
    }

    public int noOfTickMinutes() {
        return noOfTickMinutes;
    }

    public int noOfRequestedTicks() {
        return noOfRequestedTicks;
    }

    public void fillBarQuotes(final List<BarQuote> barQuotes) {
        Collections.reverse(barQuotes);
        IntStream
            .range(0, barQuotes.size())
            .forEach(index -> {
                final BarQuote bar = barQuotes.get(index);
                fillBar(bar, index * 7);
            });
    }

    private void fillBar(final BarQuote barQuote,
                         final int startIndex) {
        final IBar bar = barQuote.bar();
        final double noSpreadAvailable = 0.0;

        tickParams[startIndex] = bar.getOpen();
        tickParams[startIndex + 1] = bar.getClose();
        tickParams[startIndex + 2] = bar.getHigh();
        tickParams[startIndex + 3] = bar.getLow();
        tickParams[startIndex + 4] = TimeConvert.getUTCTimeFromBar(bar);
        tickParams[startIndex + 5] = noSpreadAvailable;
        tickParams[startIndex + 6] = bar.getVolume();
    }

    public void fillTickQuotes(final List<TickQuote> tickQuotes) {
        Collections.reverse(tickQuotes);
        IntStream
            .range(0, tickQuotes.size())
            .forEach(index -> {
                final TickQuote quote = tickQuotes.get(index);
                fillQuote(quote, index * 7);
            });
    }

    private void fillQuote(final TickQuote tickQuote,
                           final int startIndex) {
        final ITick tick = tickQuote.tick();
        final double ask = tick.getAsk();
        final double bid = tick.getBid();
        final Instrument instrument = tickQuote.instrument();

        tickParams[startIndex] = ask;
        tickParams[startIndex + 1] = ask;
        tickParams[startIndex + 2] = ask;
        tickParams[startIndex + 3] = ask;
        tickParams[startIndex + 4] = TimeConvert.getUTCTimeFromTick(tick);
        // logger.info("Stored tick with time " +
        // DateTimeUtil.formatMillis(tick.getTime()));
        tickParams[startIndex + 5] = MathUtil.roundPrice(ask - bid, instrument);
        tickParams[startIndex + 6] = tick.getAskVolume();
    }
}
