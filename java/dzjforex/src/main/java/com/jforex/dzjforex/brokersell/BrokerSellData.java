package com.jforex.dzjforex.brokersell;

import com.jforex.dzjforex.order.TradeUtility;

public class BrokerSellData {

    private final int orderID;
    private final double amount;

    public BrokerSellData(final int orderID,
                          final int contracts,
                          final TradeUtility tradeUtility) {
        this.orderID = orderID;

        amount = tradeUtility.contractsToAmount(contracts);
    }

    public int orderID() {
        return orderID;
    }

    public double amount() {
        return amount;
    }
}