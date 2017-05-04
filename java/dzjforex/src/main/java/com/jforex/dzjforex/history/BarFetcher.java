package com.jforex.dzjforex.history;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.brokerapi.BrokerHistoryData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;
import io.reactivex.Single;

public class BarFetcher {

    private final HistoryProvider historyProvider;
    private final StrategyUtil strategyUtil;
    private final PluginConfig pluginConfig;
    private final Zorro zorro;
    private List<IBar> fetchedBars;

    private final static Logger logger = LogManager.getLogger(BarFetcher.class);

    public BarFetcher(final HistoryProvider historyProvider,
                      final StrategyUtil strategyUtil,
                      final PluginConfig pluginConfig,
                      final Zorro zorro) {
        this.historyProvider = historyProvider;
        this.strategyUtil = strategyUtil;
        this.pluginConfig = pluginConfig;
        this.zorro = zorro;
    }

    public int fetch(final Instrument instrument,
                     final BrokerHistoryData brokerHistoryData) {
        final long startMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.startTime());
        long endMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.endTime());

        logger.debug("Requested bars for instrument " + instrument + ": \n "
                + "startTimeUTC: " + brokerHistoryData.startTime() + "\n "
                + "endTimeUTC: " + brokerHistoryData.endTime() + "\n "
                + "startTimeConverted: " + DateTimeUtil.formatMillis(startMillis) + "\n "
                + "endTimeConverted: " + DateTimeUtil.formatMillis(endMillis) + "\n "
                + "ntickMinutes: " + brokerHistoryData.noOfTickMinutes() + "\n "
                + "nTicks: " + brokerHistoryData.noOfTicks());

        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        final long latestTickTime = strategyUtil
            .instrumentUtil(instrument)
            .tickQuote()
            .getTime();
        if (endMillis > latestTickTime - period.getInterval()) {
            endMillis = historyProvider
                .previousBarStart(period, latestTickTime)
                .doOnSubscribe(d -> logger.debug("Trying to get previous bar time for period " + period
                        + " and current time " + DateTimeUtil.formatMillis(latestTickTime)))
                .doOnError(err -> logger.error("Getting previous bar time failed! " + err.getMessage()))
                .retryWhen(RxUtility.retryForHistory(pluginConfig))
                .onErrorResumeNext(Single.just(0L))
                .blockingGet();
            logger.debug("Adapted endMillis for " + instrument + "are " + DateTimeUtil.formatMillis(endMillis));
        }

        final long startMillisAdapted = endMillis - (brokerHistoryData.noOfTicks() - 1) * period.getInterval();
        final BarParams barParams = BarParams
            .forInstrument(instrument)
            .period(period)
            .offerSide(OfferSide.ASK);
        final long endMillisAdapted = endMillis;
        final Observable<List<IBar>> historyBars = historyProvider
            .fetchBars(barParams,
                       startMillisAdapted,
                       endMillisAdapted)
            .doOnSubscribe(d -> logger.debug("Starting to fetch bars for " + instrument + "\n"
                    + "period: " + period + "\n"
                    + "offerSide: " + OfferSide.ASK + "\n"
                    + "startTime: " + DateTimeUtil.formatMillis(startMillisAdapted) + "\n"
                    + "endTime: " + DateTimeUtil.formatMillis(endMillisAdapted)))
            .doOnSuccess(bars -> logger.debug("Fetching bars for " + instrument + " completed."))
            .doOnError(err -> logger.error("Fetch bars failed! " + err.getMessage()))
            .retryWhen(RxUtility.retryForHistory(pluginConfig))
            .onErrorResumeNext(Single.just(new ArrayList<>()))
            .toObservable();
        fetchedBars = zorro.progressWait(historyBars);

        logger.debug("Fetched " + fetchedBars.size()
                + " bars for " + instrument
                + " with nTicks " + brokerHistoryData.noOfTicks());

        return fetchedBars.isEmpty()
                ? ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()
                : fillBars(fetchedBars,
                           instrument,
                           period,
                           brokerHistoryData);
    }

    private int fillBars(final List<IBar> bars,
                         final Instrument instrument,
                         final Period period,
                         final BrokerHistoryData brokerHistoryData) {
        final List<BarQuote> barQuotes = barsToQuotes(bars,
                                                      instrument,
                                                      period);
        brokerHistoryData.fillBars(barQuotes);
        return bars.size();
    }

    private List<BarQuote> barsToQuotes(final List<IBar> bars,
                                        final Instrument instrument,
                                        final Period period) {
        return bars
            .stream()
            .map(bar -> barToQuote(bar,
                                   instrument,
                                   period))
            .collect(Collectors.toList());
    }

    private BarQuote barToQuote(final IBar bar,
                                final Instrument instrument,
                                final Period period) {
        final BarParams barParams = BarParams
            .forInstrument(instrument)
            .period(period)
            .offerSide(OfferSide.ASK);
        return new BarQuote(bar, barParams);
    }
}
