package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.time.DateTimeUtils;

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
                + "startDate: " + DateTimeUtils.formatOLETime(startDate) + ": \n "
                + "endDate: " + DateTimeUtils.formatOLETime(endDate) + ": \n "
                + "tickMinutes: " + tickMinutes + ": \n "
                + "nTicks: " + nTicks);

        final Period period = DateTimeUtils.getPeriodFromMinutes(tickMinutes);
        final long endTimeInMillis = DateTimeUtils.getMillisFromOLEDate(endDate);
        final BarFetchTimes barFetchTimes = barFetchTimeCalculator.calculate(endTimeInMillis,
                                                                             nTicks,
                                                                             period);
        final List<IBar> bars = historyProvider.fetchBars(instrument,
                                                          period,
                                                          OfferSide.ASK,
                                                          Filter.WEEKENDS,
                                                          barFetchTimes.startTime(),
                                                          barFetchTimes.endTime());
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
            tickParams[tickParamsIndex + 4] = DateTimeUtils.getUTCTimeFromBar(bar);
            // tickParams[tickParamsIndex + 5] = spread not available for bars
            tickParams[tickParamsIndex + 6] = bar.getVolume();

            tickParamsIndex += 7;
        }
        return bars.size();
    }
}
