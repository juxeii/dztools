package com.jforex.dzjforex.brokertime;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.ZorroReturnValues;

import io.reactivex.Single;

public class BrokerTime {

    private final IClient client;
    private final ServerTimeProvider serverTimeProvider;
    private final IDataService dataService;

    public BrokerTime(final IClient client,
                      final ServerTimeProvider serverTimeProvider,
                      final IDataService dataService) {
        this.client = client;
        this.serverTimeProvider = serverTimeProvider;
        this.dataService = dataService;
    }

    public Single<Integer> get(final BrokerTimeData brokerTimeData) {
        return Single.defer(() -> client.isConnected()
                ? fillServerTimeAndReturnStatus(brokerTimeData)
                : Single.just(ZorroReturnValues.CONNECTION_LOST_NEW_LOGIN_REQUIRED.getValue()));
    }

    private Single<Integer> fillServerTimeAndReturnStatus(final BrokerTimeData brokerTimeData) {
        return serverTimeProvider
            .get()
            .doOnSuccess(brokerTimeData::fill)
            .map(serverTime -> dataService.isOfflineTime(serverTime)
                    ? ZorroReturnValues.CONNECTION_OK_BUT_MARKET_CLOSED.getValue()
                    : ZorroReturnValues.CONNECTION_OK.getValue())
            .onErrorReturnItem(ZorroReturnValues.INVALID_SERVER_TIME.getValue());
    }
}
