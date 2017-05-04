package com.jforex.dzjforex.handler;

import com.dukascopy.api.IHistory;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.brokerapi.BrokerHistory;
import com.jforex.dzjforex.brokerapi.BrokerHistoryData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.history.BarFetcher;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.history.TickFetcher;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.programming.strategy.StrategyUtil;

public class HistoryHandler {

    private final BrokerHistory brokerHistory;
    private final HistoryProvider historyProvider;

    public HistoryHandler(final SystemHandler systemHandler) {
        final InfoStrategy infoStrategy = systemHandler.infoStrategy();
        final StrategyUtil strategyUtil = infoStrategy.strategyUtil();
        final IHistory history = infoStrategy.getHistory();
        final PluginConfig pluginConfig = systemHandler.pluginConfig();
        final Zorro zorro = systemHandler.zorro();
        historyProvider = new HistoryProvider(history);
        final BarFetcher barFetcher = new BarFetcher(historyProvider,
                                                     strategyUtil,
                                                     pluginConfig,
                                                     zorro);
        final TickFetcher tickFetcher = new TickFetcher(systemHandler,
                                                        historyProvider,
                                                        pluginConfig);
        brokerHistory = new BrokerHistory(barFetcher, tickFetcher);
    }

    public HistoryProvider historyProvider() {
        return historyProvider;
    }

    public int brokerHistory(final BrokerHistoryData brokerHistoryData) {
        return brokerHistory.get(brokerHistoryData);
    }
}
