package com.jforex.dzjforex.brokerstop;

public class BrokerStopData {

    private final int nTradeID;
    private final double slPrice;

    public BrokerStopData(final int nTradeID,
                          final double slPrice) {
        this.nTradeID = nTradeID;
        this.slPrice = slPrice;
    }

    public int nTradeID() {
        return nTradeID;
    }

    public double slPrice() {
        return slPrice;
    }
}
