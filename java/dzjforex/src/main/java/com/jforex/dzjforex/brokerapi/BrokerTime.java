package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.datetime.DateTimeUtils;

public class BrokerTime {

    private final IClient client;
    private final DateTimeUtils dateTimeUtils;

    public BrokerTime(final IClient client,
                      final DateTimeUtils dateTimeUtils) {
        this.client = client;
        this.dateTimeUtils = dateTimeUtils;
    }

    public int handle(final double serverTimeParams[]) {
        return client.isConnected()
                ? fillServerTimeAndReturnStatus(serverTimeParams)
                : ReturnCodes.CONNECTION_LOST_NEW_LOGIN_REQUIRED;
    }

    private int fillServerTimeAndReturnStatus(final double serverTimeParams[]) {
        final long currentServerTime = dateTimeUtils.getServerTime();
        serverTimeParams[0] = DateTimeUtils.getOLEDateFromMillis(currentServerTime);

        return dateTimeUtils.isMarketOffline(currentServerTime)
                ? ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED
                : ReturnCodes.CONNECTION_OK;
    }
}
