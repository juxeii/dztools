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

    public BarFetchTimes calculate(final double endTimeUTC,
                                   final int nTicks,
                                   final Period period) {
        final long endTimeInMillis = DateTimeUtils.millisFromOLEDate(endTimeUTC);
        final long endTimeForBar = historyProvider.getBarStart(period, endTimeInMillis);
        final long startTimeEstimation = endTimeForBar - (nTicks - 1) * period.getInterval();
        final long startTimeForBar = historyProvider.getBarStart(period, startTimeEstimation);
        logger.debug("Calculated bar fetch times: \n"
                + "endTimeForBar: " + DateTimeUtils.formatDateTime(endTimeForBar) + "\n"
                + "nTicks: " + nTicks + "\n"
                + "period: " + period + "\n"
                + "endTimeForBar: " + DateTimeUtils.formatDateTime(endTimeForBar) + "\n"
                + "startTimeForBar: " + DateTimeUtils.formatDateTime(startTimeForBar));

        return new BarFetchTimes(startTimeForBar, endTimeForBar);
    }
}
