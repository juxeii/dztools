package com.jforex.dzjforex.history;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.HistoryConfig;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.handler.InstrumentHandler;
import com.jforex.dzjforex.time.DateTimeUtils;
import com.jforex.programming.instrument.InstrumentUtil;

public class BrokerHistory2 {

    private final HistoryProvider historyProvider;

    private final static Logger logger = LogManager.getLogger(BrokerHistory2.class);

    public BrokerHistory2(final HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    public int handle(final String instrumentName,
                      final double startDate,
                      final double endDate,
                      final int tickMinutes,
                      final int nTicks,
                      final double tickParams[]) {
        logger.debug("startDate " + DateTimeUtils.formatOLETime(startDate) +
                " endDate: " + DateTimeUtils.formatOLETime(endDate) +
                "nTicks " + nTicks + " tickMinutes " + tickMinutes);
        return InstrumentHandler
            .executeForInstrument(instrumentName,
                                  instrument -> processHistory(instrument,
                                                               startDate,
                                                               endDate,
                                                               tickMinutes,
                                                               nTicks,
                                                               tickParams),
                                  Constant.HISTORY_UNAVAILABLE);
    }

    private int processHistory(final Instrument instrument,
                               final double startDate,
                               final double endDate,
                               final int tickMinutes,
                               final int nTicks,
                               final double tickParams[]) {
        final Period period = DateTimeUtils.getPeriodFromMinutes(tickMinutes);
        if (period == null) {
            logger.error("Invalid tickMinutes " + tickMinutes + " for period " + period);
            return Constant.HISTORY_UNAVAILABLE;
        }

        final long endDateTimeRounded = getEndDateTimeRounded(instrument,
                                                              period,
                                                              DateTimeUtils.getMillisFromOLEDate(endDate));
        final long startDateTimeRounded = endDateTimeRounded - (nTicks - 1) * period.getInterval();
        final List<IBar> bars = historyProvider.fetchBars(instrument,
                                                          period,
                                                          OfferSide.ASK,
                                                          Filter.WEEKENDS,
                                                          startDateTimeRounded,
                                                          endDateTimeRounded);
        if (bars.isEmpty())
            return Constant.HISTORY_UNAVAILABLE;

        fillTICKs(bars, tickParams);
        return bars.size();
    }

    private void fillTICKs(final List<IBar> bars,
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
            // tickParams[tickParamsIndex + 5] = spread();
            tickParams[tickParamsIndex + 6] = bar.getVolume();

            tickParamsIndex += 7;
        }
    }

    private long getEndDateTimeRounded(final Instrument instrument,
                                       final Period period,
                                       final long endDateTimeRaw) {
        final long endDateTimeRounded = historyProvider.getPreviousBarStart(period, endDateTimeRaw);
        logger.debug("endDateTimeRaw " + DateTimeUtils.formatDateTime(endDateTimeRaw)
                + " endDateTimeRounded " + DateTimeUtils.formatDateTime(endDateTimeRounded));
        return endDateTimeRounded;
    }

    public int doHistoryDownload() {
        final HistoryConfig historyConfig = ConfigFactory.create(HistoryConfig.class);
        final String instrumentName = historyConfig.Asset();
        final String savePath = historyConfig.Path();
        final int startYear = historyConfig.StartYear();
        final int endYear = historyConfig.EndYear();

        final Optional<Instrument> instrumentOpt = InstrumentHandler.fromName(instrumentName);
        if (!instrumentOpt.isPresent())
            return Constant.HISTORY_DOWNLOAD_FAIL;

        final Instrument instrument = instrumentOpt.get();
        final int numYears = endYear - startYear + 1;
        for (int i = 0; i < numYears; ++i) {
            final int currentYear = startYear + i;

            ZorroLogger.log("Load " + instrument + " for " + currentYear + "...");
            final List<IBar> bars = fetchBarsForYear(instrument, currentYear);
            if (bars.size() == 0) {
                ZorroLogger.log("Load " + instrument + " for " + currentYear + " failed!");
                return Constant.HISTORY_DOWNLOAD_FAIL;
            }
            ZorroLogger.log("Load " + instrument + " for " + currentYear + " OK");

            final String fileName = getBarFileName(instrument, currentYear, savePath);
            if (!isWriteBarsToFileOK(fileName, bars))
                return Constant.HISTORY_DOWNLOAD_FAIL;
        }
        return Constant.HISTORY_DOWNLOAD_OK;
    }

    private List<IBar> fetchBarsForYear(final Instrument instrument,
                                        final int year) {
        final long startTime = DateTimeUtils.getUTCYearStartTime(year);
        final long endTime = DateTimeUtils.getUTCYearEndTime(year);
        return historyProvider.fetchBars(instrument,
                                         Period.ONE_MIN,
                                         OfferSide.ASK,
                                         Filter.WEEKENDS,
                                         startTime,
                                         endTime);
    }

    private boolean isWriteBarsToFileOK(final String fileName,
                                        final List<IBar> bars) {
        Collections.reverse(bars);
        logger.info("Writing " + fileName + " ...");
        final BarFileWriter barFileWriter = new BarFileWriter(fileName, bars);
        if (!barFileWriter.isWriteBarsToTICKsFileOK())
            return false;

        logger.info("Writing " + fileName + " OK");
        return true;
    }

    private String getBarFileName(final Instrument instrument,
                                  final int year,
                                  final String histSavePath) {
        final String instrumentName = InstrumentUtil.toStringNoSeparator(instrument);
        return histSavePath + "\\" + instrumentName + "_" + year + ".bar";
    }
}
