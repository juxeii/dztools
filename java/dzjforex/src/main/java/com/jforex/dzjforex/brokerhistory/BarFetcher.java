package com.jforex.dzjforex.brokerhistory;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

import io.reactivex.Observable;
import io.reactivex.Single;

public class BarFetcher {

    private final HistoryProvider historyProvider;
    private final Zorro zorro;
    private final HistoryFetchUtility barFetchUtility;

    private final static Logger logger = LogManager.getLogger(BarFetcher.class);

    public BarFetcher(final HistoryProvider historyProvider,
                      final Zorro zorro,
                      final HistoryFetchUtility barFetchUtility) {
        this.historyProvider = historyProvider;
        this.zorro = zorro;
        this.barFetchUtility = barFetchUtility;
    }

    public int run(final Instrument instrument,
                   final BrokerHistoryData brokerHistoryData) {
        final long startMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.startTime());
        final long endMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.endTime());
        logger.debug("Requested bars for instrument " + instrument + ":\n "
                + "startTimeFromMillis: " + DateTimeUtil.formatMillis(startMillis) + "\n "
                + "endTimeFromMillis: " + DateTimeUtil.formatMillis(endMillis) + "\n "
                + "ntickMinutes: " + brokerHistoryData.noOfTickMinutes() + "\n "
                + "nTicks: " + brokerHistoryData.noOfRequestedTicks());

        final BarParams barParams = createParams(instrument, brokerHistoryData);
        final long endMillisAdapted = barFetchUtility.calculateEndTime(instrument, brokerHistoryData);
        final long startMillisAdapted = barFetchUtility.calculateStartTime(endMillisAdapted, brokerHistoryData);

        return startFetch(barParams,
                          startMillisAdapted,
                          endMillisAdapted,
                          brokerHistoryData);
    }

    private int startFetch(final BarParams barParams,
                           final long startMillisAdapted,
                           final long endMillisAdapted,
                           final BrokerHistoryData brokerHistoryData) {
        final Observable<Integer> fetchResult = historyProvider
            .fetchBars(barParams,
                       startMillisAdapted,
                       endMillisAdapted)
            .doOnSubscribe(d -> logger.debug("Starting to fetch bars for " + barParams.instrument() + ":\n"
                    + "period: " + barParams.period() + "\n"
                    + "offerSide: " + OfferSide.ASK + "\n"
                    + "startTime: " + DateTimeUtil.formatMillis(startMillisAdapted) + "\n"
                    + "endTime: " + DateTimeUtil.formatMillis(endMillisAdapted)))
            .doOnSuccess(bars -> logger.debug("Fetched " + bars.size() + " bars for "
                    + barParams.instrument() + " with nTicks "
                    + brokerHistoryData.noOfRequestedTicks()))
            .flatMap(bars -> Single.just(fillBars(bars,
                                                  barParams,
                                                  brokerHistoryData)))
            .retryWhen(historyProvider.retryForHistory())
            .doOnError(err -> logger.error("Fetching bars failed! " + err.getMessage()))
            .onErrorResumeNext(Single.just(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()))
            .toObservable();

        return zorro.progressWait(fetchResult);
    }

    public BarParams createParams(final Instrument instrument,
                                  final BrokerHistoryData brokerHistoryData) {
        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        return BarParams
            .forInstrument(instrument)
            .period(period)
            .offerSide(OfferSide.ASK);
    }

    private int fillBars(final List<IBar> bars,
                         final BarParams barParams,
                         final BrokerHistoryData brokerHistoryData) {
        final List<BarQuote> barQuotes = barFetchUtility.barsToQuotes(bars, barParams);
        brokerHistoryData.fillBarQuotes(barQuotes);
        return bars.size();
    }
}
