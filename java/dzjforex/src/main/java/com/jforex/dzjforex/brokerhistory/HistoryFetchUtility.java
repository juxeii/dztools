package com.jforex.dzjforex.brokerhistory;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Single;

public class HistoryFetchUtility {

    private final HistoryProvider historyProvider;
    private final StrategyUtil strategyUtil;

    private final static Logger logger = LogManager.getLogger(HistoryFetchUtility.class);

    public HistoryFetchUtility(final HistoryProvider historyProvider,
                               final StrategyUtil strategyUtil) {
        this.historyProvider = historyProvider;
        this.strategyUtil = strategyUtil;
    }

    public long calculateEndTime(final Instrument instrument,
                                 final BrokerHistoryData brokerHistoryData) {
        final long endTimeMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.endTime());
        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        final long latestBarStart = latestBarStart(instrument, period);
        return endTimeMillis > latestBarStart + period.getInterval()
                ? latestBarStart
                : endTimeMillis;
    }

    private long latestBarStart(final Instrument instrument,
                                final Period period) {
        return historyProvider
            .previousBarStart(period, latestTickTime(instrument))
            .retryWhen(historyProvider.retryForHistory())
            .doOnError(err -> logger.error("Getting previous bar time failed! " + err.getMessage()))
            .onErrorResumeNext(Single.just(0L))
            .blockingGet();
    }

    private long latestTickTime(final Instrument instrument) {
        return strategyUtil
            .instrumentUtil(instrument)
            .tickQuote()
            .getTime();
    }

    public long calculateStartTime(final long endTime,
                                   final BrokerHistoryData brokerHistoryData) {
        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        return endTime - (brokerHistoryData.noOfRequestedTicks() - 1) * period.getInterval();
    }

    public List<BarQuote> barsToQuotes(final List<IBar> bars,
                                       final BarParams barParams) {
        return bars
            .stream()
            .map(bar -> new BarQuote(bar, barParams))
            .collect(Collectors.toList());
    }
}
