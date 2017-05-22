package com.jforex.dzjforex.brokersell;

public class BrokerSellData {

    private final int orderID;
    private final double amount;

    public BrokerSellData(final int orderID,
                          final double amount) {
        this.orderID = orderID;
        this.amount = amount;
    }

    public int orderID() {
        return orderID;
    }

    public double amount() {
        return amount;
    }
}