package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.history.HistoryOrders;

import io.reactivex.Single;

public class OrderLookup {

    private final OrderRepository orderRepository;
    private final OpenOrders openOrders;
    private final HistoryOrders historyOrders;

    private final static Logger logger = LogManager.getLogger(OrderLookup.class);

    public OrderLookup(final OrderRepository orderRepository,
                       final OpenOrders openOrders,
                       final HistoryOrders historyOrders) {
        this.orderRepository = orderRepository;
        this.openOrders = openOrders;
        this.historyOrders = historyOrders;
    }

    public Single<IOrder> getByID(final int orderID) {
        return orderInRepository(orderID)
            .onErrorResumeNext(orderInOpenOrders(orderID))
            .onErrorResumeNext(orderInHistory(orderID));
    }

    private Single<IOrder> orderInRepository(final int orderID) {
        return Single
            .defer(() -> orderRepository.getByID(orderID))
            .doOnSubscribe(d -> logger.trace("Looking up orderID " + orderID + " in cache..."))
            .doAfterSuccess(order -> logger.trace("Found orderID " + orderID + " in cache."))
            .doOnError(e -> logger.trace("OrderID " + orderID + " not found in cache"));
    }

    private Single<IOrder> orderInOpenOrders(final int orderID) {
        return Single
            .defer(() -> openOrders.getByID(orderID))
            .doOnSubscribe(d -> logger.debug("Looking up orderID " + orderID + " in open orders..."))
            .doAfterSuccess(order -> logger.debug("Found orderID " + orderID + " in open orders."))
            .doOnError(e -> logger.debug("OrderID " + orderID + " not found in open orders."));
    }

    private Single<IOrder> orderInHistory(final int orderID) {
        return Single
            .defer(() -> historyOrders.getByID(orderID))
            .doOnSubscribe(d -> logger.debug("Looking up orderID " + orderID + " in history..."))
            .doAfterSuccess(order -> logger.debug("Found orderID " + orderID + " in history."))
            .doOnError(e -> logger.error("OrderID " + orderID + " not found in history!"));
    }
}
