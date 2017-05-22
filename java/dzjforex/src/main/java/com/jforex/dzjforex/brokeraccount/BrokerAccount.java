package com.jforex.dzjforex.brokeraccount;

import com.jforex.dzjforex.config.ZorroReturnValues;

import io.reactivex.Single;

public class BrokerAccount {

    private final AccountInfo accountInfo;

    public BrokerAccount(final AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Single<Integer> handle(final BrokerAccountData brokerAccountData) {
        return Single.fromCallable(() -> {
            if (!accountInfo.isConnected())
                return ZorroReturnValues.ACCOUNT_UNAVAILABLE.getValue();

            brokerAccountData.fill(accountInfo);
            return ZorroReturnValues.ACCOUNT_AVAILABLE.getValue();
        });
    }
}
