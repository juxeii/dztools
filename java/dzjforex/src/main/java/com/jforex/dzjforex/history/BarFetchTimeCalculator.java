package com.jforex.dzjforex.history;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Period;
import com.jforex.dzjforex.time.DateTimeUtils;

public class BarFetchTimeCalculator {

    private final HistoryProvider historyProvider;

    private final static Logger logger = LogManager.getLogger(BarFetchTimeCalculator.class);

    public BarFetchTimeCalculator(final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    public BarFetchTimes calculate(final long endTime,
                                   final int nTicks,
                                   final Period period) {
        final long endTimeRounded = historyProvider.getPreviousBarStart(period, endTime);
        final long startTimeRounded = endTimeRounded - (nTicks - 1) * period.getInterval();
        logger.debug("Calculated bar fetch times: \n"
                + "endTime raw: " + DateTimeUtils.formatDateTime(endTime) + "\n"
                + "nTicks: " + nTicks + "\n"
                + "period: " + period + "\n"
                + "endTimeRounded: " + DateTimeUtils.formatDateTime(endTimeRounded) + "\n"
                + "startTimeRounded: " + DateTimeUtils.formatDateTime(startTimeRounded));

        return new BarFetchTimes(startTimeRounded, endTimeRounded);
    }
}
