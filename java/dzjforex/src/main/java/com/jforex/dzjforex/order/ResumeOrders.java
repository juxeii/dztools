package com.jforex.dzjforex.order;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.PluginConfig;

public class ResumeOrders {

    private final static Logger logger = LogManager.getLogger(ResumeOrders.class);

    private final IEngine engine;
    private final OrderRepository orderRepository;
    private final PluginConfig pluginConfig;

    public ResumeOrders(final IEngine engine,
                        final OrderRepository orderRepository,
                        final PluginConfig pluginConfig) {
        this.engine = engine;
        this.orderRepository = orderRepository;
        this.pluginConfig = pluginConfig;
    }

    public void resume() {
        List<IOrder> orders = null;
        try {
            orders = engine.getOrders();
        } catch (final JFException e) {
            logger.error("getOrders exc: " + e.getMessage());
        }
        for (final IOrder order : orders)
            resumeOrderIDIfFound(order);
    }

    private void resumeOrderIDIfFound(final IOrder order) {
        final String label = order.getLabel();
        if (label.startsWith(pluginConfig.orderLabelPrefix())) {
            final int orderID = getOrderIDFromLabel(label);
            orderRepository.storeOrder(orderID, order);
        }
    }

    private int getOrderIDFromLabel(final String label) {
        final String idName = label.substring(pluginConfig.orderLabelPrefix().length());
        return Integer.parseInt(idName);
    }
}
