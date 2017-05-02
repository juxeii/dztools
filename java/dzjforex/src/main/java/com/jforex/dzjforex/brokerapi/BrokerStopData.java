package com.jforex.dzjforex.brokerapi;

public class BrokerStopData {

    private final int nTradeID;
    private final double stopDistance;

    public BrokerStopData(final int nTradeID,
                          final double stopDistance) {
        this.nTradeID = nTradeID;
        this.stopDistance = stopDistance;
    }

    public int nTradeID() {
        return nTradeID;
    }

    public double stopDistance() {
        return stopDistance;
    }
}
