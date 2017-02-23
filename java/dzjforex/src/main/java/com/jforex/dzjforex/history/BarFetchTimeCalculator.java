package com.jforex.dzjforex.history;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Period;
import com.jforex.dzjforex.time.TimeConvert;

public class BarFetchTimeCalculator {

    private final HistoryProvider historyProvider;

    private final static Logger logger = LogManager.getLogger(BarFetchTimeCalculator.class);

    public BarFetchTimeCalculator(final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    public BarFetchTimes calculate(final double endTimeUTC,
                                   final int nTicks,
                                   final Period period) {
        final long endTimeInMillis = TimeConvert.millisFromOLEDate(endTimeUTC);
        final long endTimeForBar = historyProvider
            .fetchBarStart(period, endTimeInMillis)
            .blockingFirst();
        final long startTimeForBar = endTimeForBar - (nTicks - 1) * period.getInterval();
        logger.debug("Calculated bar fetch times: \n"
                + "endTimeForBar: " + TimeConvert.formatDateTime(endTimeForBar) + "\n"
                + "nTicks: " + nTicks + "\n"
                + "period: " + period + "\n"
                + "endTimeForBar: " + TimeConvert.formatDateTime(endTimeForBar) + "\n"
                + "startTimeForBar: " + TimeConvert.formatDateTime(startTimeForBar));

        return new BarFetchTimes(startTimeForBar, endTimeForBar);
    }
}
