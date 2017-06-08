package com.jforex.dzjforex.brokerhistory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.misc.TimeConvert;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

public class HistoryTickFiller {

    private final double tickParams[];

    private final static Logger logger = LogManager.getLogger(HistoryTickFiller.class);

    public HistoryTickFiller(final double tickParams[]) {
        this.tickParams = tickParams;
    }

    public void fillBarQuote(final BarQuote barQuote,
                             final int startIndex) {
        final IBar bar = barQuote.bar();
        final double open = bar.getOpen();
        final double close = bar.getClose();
        final double high = bar.getHigh();
        final double low = bar.getLow();
        final double utcTime = TimeConvert.getUTCTimeFromBar(bar);
        final double spread = 0.0;
        final double volume = bar.getVolume();

        tickParams[startIndex] = open;
        tickParams[startIndex + 1] = close;
        tickParams[startIndex + 2] = high;
        tickParams[startIndex + 3] = low;
        tickParams[startIndex + 4] = utcTime;
        tickParams[startIndex + 5] = spread;
        tickParams[startIndex + 6] = volume;
        logger.trace("Stored bar for " + barQuote.instrument()
                + " open " + open
                + " close " + close
                + " high " + high
                + " low " + low
                + " time " + DateTimeUtil.formatMillis(bar.getTime())
                + " spread " + spread
                + " volume " + volume);
    }

    public void fillTickQuote(final TickQuote tickQuote,
                              final int startIndex) {
        final ITick tick = tickQuote.tick();
        final Instrument instrument = tickQuote.instrument();
        final double ask = tick.getAsk();
        final double bid = tick.getBid();
        final double utcTime = TimeConvert.getUTCTimeFromTick(tick);
        final double spread = MathUtil.roundPrice(ask - bid, instrument);
        final double volume = tick.getAskVolume();

        tickParams[startIndex] = ask;
        tickParams[startIndex + 1] = ask;
        tickParams[startIndex + 2] = ask;
        tickParams[startIndex + 3] = ask;
        tickParams[startIndex + 4] = utcTime;
        tickParams[startIndex + 5] = spread;
        tickParams[startIndex + 6] = volume;
        logger.trace("Stored tick for " + instrument
                + " ask " + ask
                + " time " + DateTimeUtil.formatMillis(tick.getTime())
                + " spread " + spread
                + " volume " + volume);
    }
}
