package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;

public class BrokerBuyData {

    private final String instrumentName;
    private final double slDistance;
    private final double tradeParams[];
    private final OrderCommand orderCommand;
    private final double amount;

    public BrokerBuyData(final String instrumentName,
                         final double amount,
                         final OrderCommand orderCommand,
                         final double slDistance,
                         final double tradeParams[]) {
        this.instrumentName = instrumentName;
        this.amount = amount;
        this.orderCommand = orderCommand;
        this.slDistance = slDistance;
        this.tradeParams = tradeParams;
    }

    public String instrumentName() {
        return instrumentName;
    }

    public double amount() {
        return amount;
    }

    public double slDistance() {
        return slDistance;
    }

    public OrderCommand orderCommand() {
        return orderCommand;
    }

    public void fillOpenPrice(final IOrder order) {
        tradeParams[0] = order.getOpenPrice();
    }
}
