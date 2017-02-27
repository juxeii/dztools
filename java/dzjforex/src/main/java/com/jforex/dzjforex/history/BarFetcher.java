package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class BarFetcher {

    private final HistoryProvider historyProvider;
    private List<IBar> fetchedBars;

    private final static Logger logger = LogManager.getLogger(BarFetcher.class);

    public BarFetcher(final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    public int fetch(final Instrument instrument,
                     final double startDate,
                     final double endDate,
                     final int tickMinutes,
                     final int nTicks,
                     final double tickParams[]) {
        final long startMillis = TimeConvert.millisFromOLEDateRoundMinutes(startDate);
        long endMillis = TimeConvert.millisFromOLEDateRoundMinutes(endDate);

        logger.debug("Requested bars for instrument " + instrument + ": \n "
                + "startDateUTCRaw: " + startDate + ": \n "
                + "endDateUTCRaw: " + endDate + ": \n "
                + "startDate: " + DateTimeUtil.formatMillis(startMillis)
                + ": \n "
                + "endDate: " + DateTimeUtil.formatMillis(endMillis)
                + ": \n "
                + "tickMinutes: " + tickMinutes + ": \n "
                + "nTicks: " + nTicks);

        final Period period = TimeConvert.getPeriodFromMinutes(tickMinutes);

        final long latestBarStart = historyProvider.latestFormedBarTime(instrument,
                                                                        period,
                                                                        OfferSide.ASK);
        if (endMillis > latestBarStart) {
            logger.warn("Latest bar time for " + instrument + " is " + DateTimeUtil.formatMillis(latestBarStart)
                    + " which is smaller than requested endDate " + DateTimeUtil.formatMillis(endMillis)
                    + " using the latest bar time now.");
            endMillis = latestBarStart;
        }

        final long startMillisAdapted = endMillis - (nTicks - 1) * period.getInterval();
        fetchedBars = null;
        historyProvider
            .fetchBars(instrument,
                       period,
                       OfferSide.ASK,
                       startMillisAdapted,
                       endMillis)
            .subscribeOn(Schedulers.io())
            .subscribe(bars -> fetchedBars = bars);

        while (fetchedBars == null) {
            Zorro.callProgress(1);
            Observable
                .interval(0L,
                          250L,
                          TimeUnit.MILLISECONDS,
                          Schedulers.io())
                .blockingFirst();
        }

        logger.debug("Fetched " + fetchedBars.size() + " bars for " + instrument + " with nTicks " + nTicks);

        return fetchedBars.isEmpty()
                ? ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()
                : fillBars(fetchedBars, tickParams);
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
