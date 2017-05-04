package com.jforex.dzjforex.brokerbuy;

public class BrokerBuyData {

    private final String instrumentName;
    private final double contracts;
    private final double stopDistance;
    private final double tradeParams[];

    public BrokerBuyData(final String instrumentName,
                         final double tradeParams[]) {
        this.instrumentName = instrumentName;
        this.tradeParams = tradeParams;

        contracts = tradeParams[0];
        stopDistance = tradeParams[1];
    }

    public String instrumentName() {
        return instrumentName;
    }

    public double contracts() {
        return contracts;
    }

    public double stopDistance() {
        return stopDistance;
    }

    public void fillOpenPrice(final double openPrice) {
        tradeParams[2] = openPrice;
    }
}
