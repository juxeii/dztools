package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.misc.DateTimeUtil;

public class BarFetcher {

    private final HistoryProvider historyProvider;
    private final BarFetchTimeCalculator barFetchTimeCalculator;

    private final static Logger logger = LogManager.getLogger(BarFetcher.class);

    public BarFetcher(final HistoryProvider historyProvider,
                      final BarFetchTimeCalculator barFetchTimeCalculator) {
        this.historyProvider = historyProvider;
        this.barFetchTimeCalculator = barFetchTimeCalculator;
    }

    public int fetch(final Instrument instrument,
                     final double startDate,
                     final double endDate,
                     final int tickMinutes,
                     final int nTicks,
                     final double tickParams[]) {
        logger.debug("Requested bars for instrument " + instrument + ": \n "
                + "startDateUTCRaw: " + startDate + ": \n "
                + "endDateUTCRaw: " + endDate + ": \n "
                + "startDate: " + DateTimeUtil.formatMillis(TimeConvert.millisFromOLEDateRoundMinutes(startDate))
                + ": \n "
                + "endDate: " + DateTimeUtil.formatMillis(TimeConvert.millisFromOLEDateRoundMinutes(endDate))
                + ": \n "
                + "tickMinutes: " + tickMinutes + ": \n "
                + "nTicks: " + nTicks);

        final Period period = TimeConvert.getPeriodFromMinutes(tickMinutes);
        final BarFetchTimes barFetchTimes = barFetchTimeCalculator.calculate(endDate,
                                                                             nTicks,
                                                                             period);
        final List<IBar> bars = historyProvider.fetchBars(instrument,
                                                          period,
                                                          OfferSide.ASK,
                                                          barFetchTimes.startTime(),
                                                          barFetchTimes.endTime());
        logger.debug("Fetched " + bars.size() + " bars for " + instrument + " with nTicks " + nTicks);
        return bars.isEmpty()
                ? ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()
                : fillBars(bars, tickParams);
    }

    private int fillBars(final List<IBar> bars,
                         final double tickParams[]) {
        int tickParamsIndex = 0;
        Collections.reverse(bars);
        for (int i = 0; i < bars.size(); ++i) {
            final IBar bar = bars.get(i);
            tickParams[tickParamsIndex] = bar.getOpen();
            tickParams[tickParamsIndex + 1] = bar.getClose();
            tickParams[tickParamsIndex + 2] = bar.getHigh();
            tickParams[tickParamsIndex + 3] = bar.getLow();
            tickParams[tickParamsIndex + 4] = TimeConvert.getUTCTimeFromBar(bar);
            // tickParams[tickParamsIndex + 5] = spread not available for bars
            tickParams[tickParamsIndex + 6] = bar.getVolume();

            tickParamsIndex += 7;
        }
        return bars.size();
    }
}
