package com.jforex.dzjforex.brokertime;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.MarketData;
import com.jforex.dzjforex.time.ServerTimeProvider;

public class BrokerTime {

    private final IClient client;
    private final ServerTimeProvider serverTimeProvider;
    private final MarketData marketData;

    public BrokerTime(final IClient client,
                      final ServerTimeProvider serverTimeProvider,
                      final MarketData marketData) {
        this.client = client;
        this.serverTimeProvider = serverTimeProvider;
        this.marketData = marketData;
    }

    public int get(final BrokerTimeData brokerTimeData) {
        return client.isConnected()
                ? fillServerTimeAndReturnStatus(brokerTimeData)
                : ZorroReturnValues.CONNECTION_LOST_NEW_LOGIN_REQUIRED.getValue();
    }

    private int fillServerTimeAndReturnStatus(final BrokerTimeData brokerTimeData) {
        return serverTimeProvider
            .get()
            .doOnSuccess(brokerTimeData::fill)
            .map(serverTime -> marketData.isMarketOffline(serverTime)
                    ? ZorroReturnValues.CONNECTION_OK_BUT_MARKET_CLOSED.getValue()
                    : ZorroReturnValues.CONNECTION_OK.getValue())
            .onErrorReturnItem(ZorroReturnValues.INVALID_SERVER_TIME.getValue())
            .blockingGet();
    }
}
