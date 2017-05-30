package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;

public class BrokerBuyData {

    private final String assetName;
    private final double slDistance;
    private final double tradeParams[];
    private final OrderCommand orderCommand;
    private final double amount;

    public BrokerBuyData(final String assetName,
                         final double amount,
                         final OrderCommand orderCommand,
                         final double slDistance,
                         final double tradeParams[]) {
        this.assetName = assetName;
        this.amount = amount;
        this.orderCommand = orderCommand;
        this.slDistance = slDistance;
        this.tradeParams = tradeParams;
    }

    public String assetName() {
        return assetName;
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
