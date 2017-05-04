package com.jforex.dzjforex.brokeraccount;

import com.jforex.dzjforex.config.ZorroReturnValues;

public class BrokerAccount {

    private final AccountInfo accountInfo;

    public BrokerAccount(final AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public int handle(final BrokerAccountData brokerAccountData) {
        if (!accountInfo.isConnected())
            return ZorroReturnValues.ACCOUNT_UNAVAILABLE.getValue();

        brokerAccountData.fill(accountInfo);
        return ZorroReturnValues.ACCOUNT_AVAILABLE.getValue();
    }
}
