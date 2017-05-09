package com.jforex.dzjforex.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.history.HistoryOrders;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class OrderRepository {

    private final OpenOrders openOrders;
    private final HistoryOrders historyOrders;
    private final OrderLabelUtil labelUtil;
    private final Map<Integer, IOrder> orderByTradeId = new HashMap<>();

    private final static Logger logger = LogManager.getLogger(OrderRepository.class);

    public OrderRepository(final OpenOrders openOrders,
                           final HistoryOrders historyOrders,
                           final OrderLabelUtil labelUtil) {
        this.openOrders = openOrders;
        this.historyOrders = historyOrders;
        this.labelUtil = labelUtil;
    }

    public Maybe<IOrder> maybeOrderByID(final int orderID) {
        return maybeOrderInCache(orderID)
            .doOnSubscribe(d -> logger.trace("Looking up orderID " + orderID + " in cache..."))
            .doAfterSuccess(order -> logger.trace("Found orderID " + orderID + " in cache."))
            .switchIfEmpty(maybeOrderInOpenOrders(orderID))
            .switchIfEmpty(maybeOrderHistory(orderID));
    }

    private Maybe<IOrder> maybeOrderInCache(final int orderID) {
        return orderByTradeId.containsKey(orderID)
                ? Maybe.just(orderByTradeId.get(orderID))
                : Maybe.empty();
    }

    private Maybe<IOrder> maybeOrderInOpenOrders(final int orderID) {
        return Maybe
            .defer(() -> {
                logger.debug("OrderID " + orderID + " not found in cache. Looking up in open orders...");
                importZorroOrders(openOrders.get());
                return maybeOrderInCache(orderID);
            })
            .doAfterSuccess(order -> logger.debug("Found orderID " + orderID + " in open orders."));
    }

    private Maybe<IOrder> maybeOrderHistory(final int orderID) {
        return Maybe
            .defer(() -> {
                logger.debug("OrderID " + orderID + " not found in open orders. Looking up in history...");
                importZorroOrders(historyOrders.get());
                return maybeOrderInCache(orderID);
            })
            .doAfterSuccess(order -> logger.debug("Found orderID " + orderID + " in history."));
    }

    private void importZorroOrders(final Single<List<IOrder>> orders) {
        orders
            .blockingGet()
            .forEach(this::filterAndImportZorroOrder);
    }

    private void filterAndImportZorroOrder(final IOrder order) {
        if (labelUtil.hasZorroPrefix(order)) {
            final int orderId = labelUtil.idFromOrder(order);
            orderByTradeId.put(orderId, order);
        }
    }
}
