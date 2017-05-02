package com.jforex.dzjforex.brokerapi;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.AccountInfo;

public class BrokerAccount {

    private final AccountInfo accountInfo;

    public BrokerAccount(final AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public int handle(final BrokerAccountData brokerAccountData) {
        return accountInfo.isConnected()
                ? fillAccountInfo(brokerAccountData)
                : ZorroReturnValues.ACCOUNT_UNAVAILABLE.getValue();
    }

    private int fillAccountInfo(final BrokerAccountData brokerAccountData) {
        brokerAccountData.fill(accountInfo);
        return ZorroReturnValues.ACCOUNT_AVAILABLE.getValue();
    }
}
