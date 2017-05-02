package com.jforex.dzjforex.brokerapi;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

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
    private final int noOfTicks;
    private final double tickParams[];

    public BrokerHistoryData(final String instrumentName,
                             final double startTime,
                             final double endTime,
                             final int noOfTickMinutes,
                             final int noOfTicks,
                             final double tickParams[]) {
        this.instrumentName = instrumentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.noOfTickMinutes = noOfTickMinutes;
        this.noOfTicks = noOfTicks;
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

    public int noOfTicks() {
        return noOfTicks;
    }

    public void fillBars(final List<BarQuote> bars) {
        final BiConsumer<BarQuote, Integer> barFiller = (bar, startIndex) -> fillBar(bar, startIndex);
        genericFill(bars, barFiller);
    }

    private void fillBar(final BarQuote barQuote,
                         final int startIndex) {
        final IBar bar = barQuote.bar();
        tickParams[startIndex] = bar.getOpen();
        tickParams[startIndex + 1] = bar.getClose();
        tickParams[startIndex + 2] = bar.getHigh();
        tickParams[startIndex + 3] = bar.getLow();
        tickParams[startIndex + 4] = TimeConvert.getUTCTimeFromBar(bar);
        // tickParams[tickParamsIndex + 5] = spread not available for bars
        tickParams[startIndex + 6] = bar.getVolume();
    }

    public void fillTicks(final List<TickQuote> ticks) {
        final BiConsumer<TickQuote, Integer> tickFiller = (tick, startIndex) -> fillTick(tick, startIndex);
        genericFill(ticks, tickFiller);
    }

    private void fillTick(final TickQuote tickQuote,
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
        tickParams[startIndex + 5] = MathUtil.roundPrice(ask - bid, instrument);
        tickParams[startIndex + 6] = tick.getAskVolume();
    }

    private <T> void genericFill(final List<T> ticks,
                                 final BiConsumer<T, Integer> tickFiller) {
        int startIndex = 0;
        Collections.reverse(ticks);
        for (int i = 0; i < ticks.size(); ++i) {
            final T tick = ticks.get(i);
            tickFiller.accept(tick, startIndex);
            startIndex += 7;
        }
    }
}
