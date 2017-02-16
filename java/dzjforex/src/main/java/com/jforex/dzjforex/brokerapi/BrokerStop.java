package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.OrderHandler;

public class BrokerStop {

    private final OrderHandler orderHandler;
    private final AccountInfo accountInfo;

    public BrokerStop(final OrderHandler orderHandler,
                      final AccountInfo accountInfo) {
        this.orderHandler = orderHandler;
        this.accountInfo = accountInfo;
    }

    public int handle(final int orderID,
                      final double newSLPrice) {
        if (!accountInfo.isTradingAllowed() || !orderHandler.isOrderKnown(orderID))
            return ReturnCodes.UNKNOWN_ORDER_ID;

        final IOrder order = orderHandler.getOrder(orderID);
        return orderHandler.setSLPrice(order, newSLPrice);
    }
}
