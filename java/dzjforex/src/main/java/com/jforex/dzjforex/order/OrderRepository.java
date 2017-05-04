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
            .switchIfEmpty(maybeOrderAfterImport(orderID, openOrders.get()))
            .doOnSubscribe(d -> logger.trace("Looking up orderID " + orderID + " in open orders..."))
            .doAfterSuccess(order -> logger.trace("Found orderID " + orderID + " in open orders."))
            .switchIfEmpty(maybeOrderAfterImport(orderID, historyOrders.get()))
            .doOnSubscribe(d -> logger.trace("Looking up orderID " + orderID + " in history..."))
            .doAfterSuccess(order -> logger.trace("Found orderID " + orderID + " in history."));
    }

    private Maybe<IOrder> maybeOrderInCache(final int orderID) {
        return orderByTradeId.containsKey(orderID)
                ? Maybe.empty()
                : Maybe.just(orderByTradeId.get(orderID));
    }

    private Maybe<IOrder> maybeOrderAfterImport(final int orderID,
                                                final Single<List<IOrder>> ordersSupplier) {
        importZorroOrders(ordersSupplier);
        return maybeOrderInCache(orderID);
    }

    public void importZorroOrders(final Single<List<IOrder>> orders) {
        orders
            .blockingGet()
            .forEach(this::filterAndImportZorroOrder);
    }

    private void filterAndImportZorroOrder(final IOrder order) {
        if (labelUtil.hasZorroPrefix(order)) {
            final String orderLabel = order.getLabel();
            if (orderLabel != null) {
                final int orderId = labelUtil.idFromLabel(orderLabel);
                orderByTradeId.put(orderId, order);
            }
        }
    }
}
