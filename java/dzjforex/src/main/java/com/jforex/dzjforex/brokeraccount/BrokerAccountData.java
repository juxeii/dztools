package com.jforex.dzjforex.brokeraccount;

public class BrokerAccountData {

    private final double accountInfoParams[];

    public BrokerAccountData(final double accountInfoParams[]) {
        this.accountInfoParams = accountInfoParams;
    }

    public void fill(final AccountInfo accountInfo) {
        accountInfoParams[0] = accountInfo.baseEquity();
        accountInfoParams[1] = accountInfo.tradeValue();
        accountInfoParams[2] = accountInfo.usedMargin();
    }
}
