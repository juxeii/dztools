package com.jforex.dzjforex.order;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.history.HistoryProvider;

public class OrderRepository {

    private final IEngine engine;
    private final HistoryProvider historyProvider;
    private final HashMap<Integer, IOrder> orderMap;

    private final static Logger logger = LogManager.getLogger(OrderRepository.class);

    public OrderRepository(final IEngine engine,
                           final HistoryProvider historyProvider) {
        this.engine = engine;
        this.historyProvider = historyProvider;

        orderMap = new HashMap<Integer, IOrder>();
    }

    public IOrder orderByID(final int orderID) {
        logger.trace("Looking up orderID " + orderID + " in cache...");
        if (isOrderIDKnown(orderID)) {
            final IOrder order = orderMap.get(orderID);
            logger.trace("Found orderID " + orderID
                    + " in cache with order label " + order.getLabel());
            return order;
        }
        return orderFromEngine(orderID);
    }

    private IOrder orderFromEngine(final int orderID) {
        logger.debug("Seeking orderID " + orderID + " in live engine...");
        final IOrder order = engine.getOrderById(String.valueOf(orderID));
        if (order == null) {
            logger.error("Order with ID " + orderID + " not found in live engine. Seeking order ID in history now.");
            return orderFromHistory(orderID);
        }

        logger.debug("Found order ID " + orderID + " in live engine with label " + order.getLabel());
        storeOrder(orderID, order);
        return order;
    }

    private IOrder orderFromHistory(final int orderID) {
        final IOrder order = historyProvider.orderByID(orderID);
        if (order != null)
            storeOrder(orderID, order);
        return order;
    }

    public void storeOrder(final int orderID,
                           final IOrder order) {
        orderMap.put(orderID, order);
    }

    private boolean isOrderIDKnown(final int orderID) {
        return orderMap.containsKey(orderID);
    }
}
