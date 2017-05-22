package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IOrder;

public class BrokerBuyData {

    private final String instrumentName;
    private final int nAmount;
    private final double dStopDist;
    private final double tradeParams[];

    public BrokerBuyData(final String instrumentName,
                         final int nAmount,
                         final double dStopDist,
                         final double tradeParams[]) {
        this.instrumentName = instrumentName;
        this.nAmount = nAmount;
        this.dStopDist = dStopDist;
        this.tradeParams = tradeParams;
    }

    public String instrumentName() {
        return instrumentName;
    }

    public int nAmount() {
        return nAmount;
    }

    public double dStopDist() {
        return dStopDist;
    }

    public void fillOpenPrice(final IOrder order) {
        tradeParams[0] = order.getOpenPrice();
    }
}
