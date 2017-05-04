package com.jforex.dzjforex.brokertrade;

public class BrokerTradeData {

    private final int nTradeID;
    private final double tradeParams[];

    public BrokerTradeData(final int nTradeID,
                           final double tradeParams[]) {
        this.nTradeID = nTradeID;
        this.tradeParams = tradeParams;
    }

    public int nTradeID() {
        return nTradeID;
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
