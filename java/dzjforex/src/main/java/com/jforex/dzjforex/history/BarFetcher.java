package com.jforex.dzjforex.history;

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
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.time.TimeConvert;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.strategy.StrategyUtil;

public class BarFetcher {

    private final HistoryProvider historyProvider;
    private final StrategyUtil strategyUtil;
    private final Zorro zorro;
    private List<IBar> fetchedBars;

    private final static Logger logger = LogManager.getLogger(BarFetcher.class);

    public BarFetcher(final HistoryProvider historyProvider,
                      final StrategyUtil strategyUtil,
                      final Zorro zorro) {
        this.historyProvider = historyProvider;
        this.strategyUtil = strategyUtil;
        this.zorro = zorro;
    }

    public int fetch(final Instrument instrument,
                     final BrokerHistoryData brokerHistoryData) {
        final long startMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.startTime());
        long endMillis = TimeConvert.millisFromOLEDateRoundMinutes(brokerHistoryData.endTime());

        logger.debug("Requested bars for instrument " + instrument + ": \n "
                + "startDateUTCRaw: " + brokerHistoryData.startTime() + ": \n "
                + "endDateUTCRaw: " + brokerHistoryData.endTime() + ": \n "
                + "startDate: " + DateTimeUtil.formatMillis(startMillis)
                + ": \n "
                + "endDate: " + DateTimeUtil.formatMillis(endMillis)
                + ": \n "
                + "tickMinutes: " + brokerHistoryData.noOfTickMinutes() + ": \n "
                + "nTicks: " + brokerHistoryData.noOfTicks());

        final Period period = TimeConvert.getPeriodFromMinutes(brokerHistoryData.noOfTickMinutes());
        final long latestTickTime = strategyUtil
            .instrumentUtil(instrument)
            .tickQuote()
            .getTime();
        if (endMillis > latestTickTime - period.getInterval()) {
            endMillis = historyProvider.previousBarStart(period, latestTickTime);
            logger.debug("Adapted endMillis for " + instrument + "are " + DateTimeUtil.formatMillis(endMillis));
        }

        final long startMillisAdapted = endMillis - (brokerHistoryData.noOfTicks() - 1) * period.getInterval();
        fetchedBars = zorro.progressWait(historyProvider.fetchBars(instrument,
                                                                   period,
                                                                   OfferSide.ASK,
                                                                   startMillisAdapted,
                                                                   endMillis));

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
            .map(bar -> barToQuote(bar, instrument, period))
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
