package com.jforex.dzjforex.handler;

import java.time.Clock;

import com.jforex.dzjforex.brokerapi.BrokerTime;
import com.jforex.dzjforex.brokerapi.BrokerTimeData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.misc.MarketData;
import com.jforex.dzjforex.time.NTPFetch;
import com.jforex.dzjforex.time.NTPProvider;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.dzjforex.time.TickTimeProvider;
import com.jforex.programming.quote.TickQuoteRepository;

public class TimeHandler {

    private final Clock clock;
    private final ServerTimeProvider serverTimeProvider;
    private final BrokerTime brokerTime;

    public TimeHandler(final SystemHandler systemHandler) {
        final PluginConfig pluginConfig = systemHandler.pluginConfig();
        final InfoStrategy infoStrategy = systemHandler.infoStrategy();
        final NTPFetch ntpFetch = new NTPFetch(pluginConfig);
        final NTPProvider ntpProvider = new NTPProvider(ntpFetch, pluginConfig);
        final MarketData marketData = new MarketData(infoStrategy
            .getContext()
            .getDataService());
        final TickQuoteRepository tickQuoteRepository = infoStrategy
            .strategyUtil()
            .tickQuoteProvider()
            .repository();
        clock = systemHandler.clock();
        final TickTimeProvider tickTimeProvider = new TickTimeProvider(tickQuoteRepository, clock);
        serverTimeProvider = new ServerTimeProvider(ntpProvider,
                                                    tickTimeProvider,
                                                    clock);
        brokerTime = new BrokerTime(systemHandler.client(),
                                    serverTimeProvider,
                                    marketData);
    }

    public Clock clock() {
        return clock;
    }

    public ServerTimeProvider serverTimeProvider() {
        return serverTimeProvider;
    }

    public int brokerTime(final BrokerTimeData brokerTimeData) {
        return brokerTime.get(brokerTimeData);
    }
}
