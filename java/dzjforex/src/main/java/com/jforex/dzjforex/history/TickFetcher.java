package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.SystemHandler;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class TickFetcher {

    private final HistoryProvider historyProvider;
    private final StrategyUtil strategyUtil;
    private final Zorro zorro;
    private final long tickFetchMillis;

    private final static Logger logger = LogManager.getLogger(TickFetcher.class);

    public TickFetcher(final SystemHandler systemHandler,
                       final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
        strategyUtil = systemHandler
            .infoStrategy()
            .strategyUtil();
        zorro = systemHandler.zorro();
        tickFetchMillis = 1000 * 60 * systemHandler
            .pluginConfig()
            .tickFetchMinutes();
    }

    public int fetch(final Instrument instrument,
                     final double startDate,
                     final double endDate,
                     final int tickMinutes,
                     final int nTicks,
                     final double tickParams[]) {
        logger.debug("Trying to fetch ticks for instrument " + instrument + ": \n "
                + "startDate: " + TimeConvert.formatOLETime(startDate) + ": \n "
                + "endDate: " + TimeConvert.formatOLETime(endDate) + ": \n "
                + "tickMinutes: " + tickMinutes + ": \n "
                + "nTicks: " + nTicks);

        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);
        final long latestTickTime = instrumentUtil
            .tickQuote()
            .getTime();
        final long endDateMillis = TimeConvert.millisFromOLEDate(endDate);
        final long to = endDateMillis > latestTickTime
                ? latestTickTime
                : endDateMillis;

        final long from = TimeConvert.millisFromOLEDate(startDate);
        final List<ITick> fetchedTicks = fetchInLoop(instrument,
                                                     from,
                                                     to,
                                                     nTicks);

        logger.debug("Fetched " + fetchedTicks.size() + " ticks for " + instrument + " with nTicks " + nTicks);

        return fetchedTicks.isEmpty()
                ? ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()
                : fillTicks(instrument,
                            fetchedTicks,
                            nTicks,
                            from,
                            tickParams);
    }

    private List<ITick> fetchInLoop(final Instrument instrument,
                                    final long from,
                                    final long to,
                                    final int nTicks) {
        final List<ITick> loopTicks = new ArrayList<>();
        long dynamicTo = to;
        long dynamicFrom = to - tickFetchMillis;

        while (loopTicks.size() < nTicks && dynamicTo > from) {
            final List<ITick> tmpTicks = zorro.progressWait(historyProvider.fetchTicks(instrument,
                                                                                       dynamicFrom,
                                                                                       dynamicTo));
            if (!tmpTicks.isEmpty()) {
                Collections.reverse(tmpTicks);
                loopTicks.addAll(tmpTicks);
            }

            dynamicTo = dynamicFrom - 1;
            dynamicFrom = dynamicTo - tickFetchMillis;
        }
        return loopTicks;
    }

    private int fillTicks(final Instrument instrument,
                          final List<ITick> ticks,
                          final int nTicks,
                          final long from,
                          final double tickParams[]) {
        int tickParamsIndex = 0;
        final int toProgressSize = ticks.size() <= nTicks ? ticks.size() : nTicks;

        for (int i = 0; i < toProgressSize; ++i) {
            final ITick tick = ticks.get(i);
            final long tickTime = tick.getTime();
            if (tickTime < from) {
                return i + 1;
            }

            final double ask = tick.getAsk();
            final double spread = MathUtil.roundPrice(ask - tick.getBid(), instrument);

            tickParams[tickParamsIndex] = ask;
            tickParams[tickParamsIndex + 1] = ask;
            tickParams[tickParamsIndex + 2] = ask;
            tickParams[tickParamsIndex + 3] = ask;
            tickParams[tickParamsIndex + 4] = TimeConvert.getUTCTimeFromTick(tick);
            tickParams[tickParamsIndex + 5] = spread;
            tickParams[tickParamsIndex + 6] = tick.getAskVolume();

            tickParamsIndex += 7;
        }
        return toProgressSize;
    }
}
