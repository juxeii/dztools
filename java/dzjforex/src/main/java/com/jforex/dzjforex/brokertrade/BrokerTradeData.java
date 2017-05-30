package com.jforex.dzjforex.brokertrade;

public class BrokerTradeData {

    private final int orderID;
    private final double tradeParams[];

    public BrokerTradeData(final int orderID,
                           final double tradeParams[]) {
        this.orderID = orderID;
        this.tradeParams = tradeParams;
    }

    public int orderID() {
        return orderID;
    }

    public void fill(final double pOpen,
                     final double pClose,
                     final double pRoll,
                     final double pProfit) {
        tradeParams[0] = pOpen;
        tradeParams[1] = pClose;
        tradeParams[2] = pRoll;
        tradeParams[3] = pProfit;
    }
}
