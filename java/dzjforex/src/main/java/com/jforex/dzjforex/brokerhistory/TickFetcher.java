package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.TickQuote;

import io.reactivex.Observable;
import io.reactivex.Single;

public class TickFetcher {

    private final HistoryProvider historyProvider;
    private final Zorro zorro;

    private final static Logger logger = LogManager.getLogger(TickFetcher.class);

    public TickFetcher(final HistoryProvider historyProvider,
                       final Zorro zorro) {
        this.historyProvider = historyProvider;
        this.zorro = zorro;
    }

    public int run(final Instrument instrument,
                   final BrokerHistoryData brokerHistoryData) {
        final int noOfRequestedTicks = brokerHistoryData.noOfRequestedTicks();
        final long startDateMillis = TimeConvert.millisFromOLEDate(brokerHistoryData.startTime());
        final long endDateMillis = TimeConvert.millisFromOLEDate(brokerHistoryData.endTime()) - 2;

        final Observable<Integer> fetchResult = historyProvider
            .ticksByShift(instrument,
                          endDateMillis,
                          noOfRequestedTicks - 1)
            .doOnSubscribe(d -> logger.debug("Trying to fetch ticks for instrument " + instrument + ":\n "
                    + "startDate: " + DateTimeUtil.formatMillis(startDateMillis) + "\n "
                    + "endDate: " + DateTimeUtil.formatMillis(endDateMillis) + "\n "
                    + "tickMinutes: " + brokerHistoryData.noOfTickMinutes() + "\n "
                    + "noOfRequestedTicks: " + noOfRequestedTicks))
            .flattenAsObservable(ticks -> ticks)
            .filter(tick -> tick.getTime() >= startDateMillis)
            .map(tick -> new TickQuote(instrument, tick))
            .toList()
            .doOnSuccess(ticks -> {
                logger.debug("Fetched " + ticks.size() + " ticks for " + instrument);
                brokerHistoryData.fillTickQuotes(ticks);
            })
            .map(List::size)
            .retryWhen(historyProvider.retryForHistory())
            .doOnError(err -> logger.error("Fetching ticks failed! " + err.getMessage()))
            .onErrorResumeNext(Single.just(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()))
            .toObservable();

        return zorro.progressWait(fetchResult);
    }
}
