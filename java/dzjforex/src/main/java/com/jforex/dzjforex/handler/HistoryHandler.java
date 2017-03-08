package com.jforex.dzjforex.handler;

import com.dukascopy.api.IHistory;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.brokerapi.BrokerHistory2;
import com.jforex.dzjforex.history.BarFetcher;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.history.TickFetcher;
import com.jforex.dzjforex.misc.InfoStrategy;

public class HistoryHandler {

    private final BrokerHistory2 brokerHistory2;
    private final HistoryProvider historyProvider;

    public HistoryHandler(final SystemHandler systemHandler) {
        final InfoStrategy infoStrategy = systemHandler.infoStrategy();
        final IHistory history = infoStrategy.getHistory();
        final Zorro zorro = systemHandler.zorro();
        historyProvider = new HistoryProvider(history, systemHandler.pluginConfig());
        final BarFetcher barFetcher = new BarFetcher(historyProvider,
                                                     infoStrategy.strategyUtil(),
                                                     zorro);
        final TickFetcher tickFetcher = new TickFetcher(historyProvider, zorro);
        brokerHistory2 = new BrokerHistory2(barFetcher, tickFetcher);
    }

    public HistoryProvider historyProvider() {
        return historyProvider;
    }

    public int brokerHistory2(final String instrumentName,
                              final double startDate,
                              final double endDate,
                              final int tickMinutes,
                              final int nTicks,
                              final double tickParams[]) {
        return brokerHistory2.get(instrumentName,
                                  startDate,
                                  endDate,
                                  tickMinutes,
                                  nTicks,
                                  tickParams);
    }
}
