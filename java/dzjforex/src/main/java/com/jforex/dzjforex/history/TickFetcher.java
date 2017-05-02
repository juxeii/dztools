package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.brokerapi.BrokerHistoryData;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.SystemHandler;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.quote.TickQuote;
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
                     final BrokerHistoryData brokerHistoryData) {
        logger.debug("Trying to fetch ticks for instrument " + instrument + ": \n "
                + "startDate: " + TimeConvert.formatOLETime(brokerHistoryData.startTime()) + ": \n "
                + "endDate: " + TimeConvert.formatOLETime(brokerHistoryData.endTime()) + ": \n "
                + "tickMinutes: " + brokerHistoryData.noOfTickMinutes() + ": \n "
                + "nTicks: " + brokerHistoryData.noOfTicks());

        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);
        final long latestTickTime = instrumentUtil
            .tickQuote()
            .getTime();
        final long endDateMillis = TimeConvert.millisFromOLEDate(brokerHistoryData.endTime());
        final long to = endDateMillis > latestTickTime
                ? latestTickTime
                : endDateMillis;

        final long from = TimeConvert.millisFromOLEDate(brokerHistoryData.startTime());
        final List<ITick> fetchedTicks = fetchInLoop(instrument,
                                                     from,
                                                     to,
                                                     brokerHistoryData.noOfTicks());

        logger.debug("Fetched " + fetchedTicks.size() + " ticks for " + instrument
                + " with nTicks " + brokerHistoryData.noOfTicks());

        return fetchedTicks.isEmpty()
                ? ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()
                : fillTicks(instrument,
                            fetchedTicks,
                            from,
                            brokerHistoryData);
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
                loopTicks.addAll(tmpTicks);
            }

            dynamicTo = dynamicFrom - 1;
            dynamicFrom = dynamicTo - tickFetchMillis;
        }
        return loopTicks;
    }

    private int fillTicks(final Instrument instrument,
                          final List<ITick> ticks,
                          final long from,
                          final BrokerHistoryData brokerHistoryData) {
        int noOfTicksToFill = 0;
        final int noOfTicks = brokerHistoryData.noOfTicks();
        final int toProgressSize = ticks.size() <= noOfTicks ? ticks.size() : noOfTicks;
        final List<TickQuote> tickQuotes = new ArrayList<>();

        for (int i = 0; i < toProgressSize; ++i) {
            final ITick tick = ticks.get(i);
            if (tick.getTime() >= from) {
                ++noOfTicksToFill;
                final TickQuote tickQuote = new TickQuote(instrument, tick);
                tickQuotes.add(tickQuote);
            }
        }
        brokerHistoryData.fillTicks(tickQuotes);
        return noOfTicksToFill;
    }
}
