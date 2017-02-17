package com.jforex.dzjforex.handler;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.strategy.StrategyUtil;

public class OrderHandler {

    private final IEngine engine;
    private final HashMap<Integer, IOrder> orderMap;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(OrderHandler.class);

    public OrderHandler(final IContext context,
                        final StrategyUtil strategyUtil,
                        final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        this.engine = context.getEngine();

        orderMap = new HashMap<Integer, IOrder>();
        resumeOrderIDs();
    }

    public void storeOrder(final int orderID,
                           final IOrder order) {
        orderMap.put(orderID, order);
    }

    public IOrder getOrder(final int orderID) {
        return orderMap.get(orderID);
    }

    public int createID() {
        return Math.abs(new UID().hashCode());
    }

    public int scaleAmount(final double amount) {
        return (int) (amount * pluginConfig.LOT_SCALE());
    }

    public boolean isOrderKnown(final int orderID) {
        if (!orderMap.containsKey(orderID)) {
            logger.error("OrderID " + orderID + " is unknown!");
            return false;
        }
        return true;
    }

    private synchronized void resumeOrderIDs() {
        List<IOrder> orders = null;
        try {
            orders = engine.getOrders();
        } catch (final JFException e) {
            logger.error("getOrders exc: " + e.getMessage());
            ZorroLogger.indicateError();
        }
        for (final IOrder order : orders)
            resumeOrderIDIfFound(order);
    }

    private void resumeOrderIDIfFound(final IOrder order) {
        final String label = order.getLabel();
        if (label.startsWith(pluginConfig.ORDER_PREFIX_LABEL())) {
            final int id = getOrderIDFromLabel(label);
            orderMap.put(id, order);
        }
    }

    private int getOrderIDFromLabel(final String label) {
        final String idName = label.substring(pluginConfig.ORDER_PREFIX_LABEL().length());
        return Integer.parseInt(idName);
    }
}
