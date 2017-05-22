package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;

public class BrokerBuyData {

    private final String instrumentName;
    private final double dStopDist;
    private final double tradeParams[];
    private final OrderCommand orderCommand;
    private final double amount;

    public BrokerBuyData(final String instrumentName,
                         final double amount,
                         final OrderCommand orderCommand,
                         final double dStopDist,
                         final double tradeParams[]) {
        this.instrumentName = instrumentName;
        this.amount = amount;
        this.orderCommand = orderCommand;
        this.dStopDist = dStopDist;
        this.tradeParams = tradeParams;
    }

    public String instrumentName() {
        return instrumentName;
    }

    public double amount() {
        return amount;
    }

    public double dStopDist() {
        return dStopDist;
    }

    public OrderCommand orderCommand() {
        return orderCommand;
    }

    public void fillOpenPrice(final IOrder order) {
        tradeParams[0] = order.getOpenPrice();
    }
}
