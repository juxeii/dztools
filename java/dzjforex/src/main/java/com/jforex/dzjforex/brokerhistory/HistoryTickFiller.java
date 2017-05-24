package com.jforex.dzjforex.brokerhistory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.time.TimeConvert;
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
        final double noSpreadAvailable = 0.0;

        tickParams[startIndex] = bar.getOpen();
        tickParams[startIndex + 1] = bar.getClose();
        tickParams[startIndex + 2] = bar.getHigh();
        tickParams[startIndex + 3] = bar.getLow();
        tickParams[startIndex + 4] = TimeConvert.getUTCTimeFromBar(bar);
        logger.debug("Stored bar time time " + DateTimeUtil.formatMillis(bar.getTime()));
        tickParams[startIndex + 5] = noSpreadAvailable;
        tickParams[startIndex + 6] = bar.getVolume();
    }

    public void fillTickQuote(final TickQuote tickQuote,
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
        logger.debug("Stored tick with time " + DateTimeUtil.formatMillis(tick.getTime()));
        tickParams[startIndex + 5] = MathUtil.roundPrice(ask - bid, instrument);
        tickParams[startIndex + 6] = tick.getAskVolume();
    }
}
