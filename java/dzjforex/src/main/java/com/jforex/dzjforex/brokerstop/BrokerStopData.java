package com.jforex.dzjforex.brokerstop;

public class BrokerStopData {

    private final int orderID;
    private final double slPrice;

    public BrokerStopData(final int orderID,
                          final double slPrice) {
        this.orderID = orderID;
        this.slPrice = slPrice;
    }

    public int orderID() {
        return orderID;
    }

    public double slPrice() {
        return slPrice;
    }
}
