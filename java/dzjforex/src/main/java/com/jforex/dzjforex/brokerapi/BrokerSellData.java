package com.jforex.dzjforex.brokerapi;

public class BrokerSellData {

    private final int nTradeID;
    private final int nAmount;

    public BrokerSellData(final int nTradeID,
                          final int nAmount) {
        this.nTradeID = nTradeID;
        this.nAmount = nAmount;
    }

    public int nTradeID() {
        return nTradeID;
    }

    public int nAmount() {
        return nAmount;
    }
}