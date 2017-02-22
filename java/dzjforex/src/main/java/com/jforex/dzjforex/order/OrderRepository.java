package com.jforex.dzjforex.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;

public class OrderRepository {

    private final RunningOrders runningOrders;
    private final HistoryOrders historyOrders;
    private final OrderLabelUtil labelUtil;
    private final Map<Integer, IOrder> orderByTradeId = new HashMap<>();

    private final static Logger logger = LogManager.getLogger(OrderRepository.class);

    public OrderRepository(final RunningOrders runningOrders,
                           final HistoryOrders historyOrders,
                           final OrderLabelUtil labelUtil) {
        this.runningOrders = runningOrders;
        this.historyOrders = historyOrders;
        this.labelUtil = labelUtil;
    }

    public IOrder orderByID(final int orderID) {
        logger.trace("Looking up orderID " + orderID + " in cache...");
        if (!orderByTradeId.containsKey(orderID)) {
            logger.debug("Order with ID " + orderID + " not found cache. Looking up in running orders now...");
            return orderFromEngine(orderID);
        }
        logger.trace("Found orderID " + orderID + " in cache.");
        return orderByTradeId.get(orderID);
    }

    private IOrder orderFromEngine(final int orderID) {
        logger.debug("Seeking orderID " + orderID + " in running orders...");
        final List<IOrder> orders = runningOrders.get();
        importZorroOrders(orders);

        if (!orderByTradeId.containsKey(orderID)) {
            logger.debug("Order with ID " + orderID + " not found in running orders. Looking up in history now...");
            return orderFromHistory(orderID);
        }
        logger.debug("Found orderID " + orderID + " in running orders.");
        return orderByTradeId.get(orderID);
    }

    private IOrder orderFromHistory(final int orderID) {
        logger.debug("Seeking orderID " + orderID + " in history...");
        final List<IOrder> orders = historyOrders.get();
        importZorroOrders(orders);

        if (!orderByTradeId.containsKey(orderID)) {
            logger.error("Order with ID " + orderID + " not found in history. Returning null!");
            return null;
        }
        return orderByTradeId.get(orderID);
    }

    public void importZorroOrders(final List<IOrder> orders) {
        orders.forEach(order -> {
            if (labelUtil.hasZorroPrefix(order)) {
                final String orderLabel = order.getLabel();
                if (orderLabel != null) {
                    final int orderId = labelUtil.idFromLabel(orderLabel);
                    storeOrder(orderId, order);
                }
            }
        });
    }

    public void storeOrder(final int orderID,
                           final IOrder order) {
        orderByTradeId.put(orderID, order);
    }
}
