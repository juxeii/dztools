package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.time.DateTimeUtils;
import com.jforex.programming.math.MathUtil;

public class TickFetcher {

    private final HistoryProvider historyProvider;

    private final static Logger logger = LogManager.getLogger(TickFetcher.class);

    public TickFetcher(final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    public int fetch(final Instrument instrument,
                     final double startDate,
                     final double endDate,
                     final int tickMinutes,
                     final int nTicks,
                     final double tickParams[]) {
        logger.debug("Trying to fetch ticks for instrument " + instrument + ": \n "
                + "startDate: " + DateTimeUtils.formatOLETime(startDate) + ": \n "
                + "endDate: " + DateTimeUtils.formatOLETime(endDate) + ": \n "
                + "tickMinutes: " + tickMinutes + ": \n "
                + "nTicks: " + nTicks);

        final List<ITick> ticks = historyProvider.fetchTicks(instrument,
                                                             DateTimeUtils.getMillisFromOLEDate(startDate),
                                                             DateTimeUtils.getMillisFromOLEDate(endDate));
        return ticks.isEmpty()
                ? ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()
                : fillTicks(instrument,
                            ticks,
                            tickParams);
    }

    private int fillTicks(final Instrument instrument,
                          final List<ITick> ticks,
                          final double tickParams[]) {
        int tickParamsIndex = 0;
        Collections.reverse(ticks);

        for (int i = 0; i < ticks.size(); ++i) {
            final ITick tick = ticks.get(i);
            final double ask = tick.getAsk();
            final double spread = MathUtil.roundPrice(ask - tick.getBid(), instrument);

            tickParams[tickParamsIndex] = ask;
            tickParams[tickParamsIndex + 1] = ask;
            tickParams[tickParamsIndex + 2] = ask;
            tickParams[tickParamsIndex + 3] = ask;
            tickParams[tickParamsIndex + 4] = DateTimeUtils.getUTCTimeFromTick(tick);
            tickParams[tickParamsIndex + 5] = spread;
            tickParams[tickParamsIndex + 6] = tick.getAskVolume();

            tickParamsIndex += 7;
        }
        return ticks.size();
    }
}
