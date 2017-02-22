package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.time.DateTimeUtils;
import com.jforex.dzjforex.time.ServerTimeProvider;

public class BrokerTime {

    private final IClient client;
    private final ServerTimeProvider serverTimeProvider;
    private final DateTimeUtils dateTimeUtils;

    public BrokerTime(final IClient client,
                      final ServerTimeProvider serverTimeProvider,
                      final DateTimeUtils dateTimeUtils) {
        this.client = client;
        this.serverTimeProvider = serverTimeProvider;
        this.dateTimeUtils = dateTimeUtils;
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        return client.isConnected()
                ? fillServerTimeAndReturnStatus(pTimeUTC)
                : ZorroReturnValues.CONNECTION_LOST_NEW_LOGIN_REQUIRED.getValue();
    }

    private int fillServerTimeAndReturnStatus(final double pTimeUTC[]) {
        final long serverTime = serverTimeProvider.get();
        pTimeUTC[0] = DateTimeUtils.getOLEDateFromMillis(serverTime);

        return dateTimeUtils.isMarketOffline(serverTime)
                ? ZorroReturnValues.CONNECTION_OK_BUT_MARKET_CLOSED.getValue()
                : ZorroReturnValues.CONNECTION_OK.getValue();
    }
}
