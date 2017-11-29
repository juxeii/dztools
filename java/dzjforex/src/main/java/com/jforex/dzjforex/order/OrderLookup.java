package com.jforex.dzjforex.order;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;

import io.reactivex.Maybe;

public class OrderLookup {

    private final OrderRepository orderRepository;
    private final OrderIDLookUp openOrders;
    private final OrderIDLookUp historyOrders;

    private final static Logger logger = LogManager.getLogger(OrderLookup.class);

    public OrderLookup(final OrderRepository orderRepository,
                       final OrderIDLookUp openOrders,
                       final OrderIDLookUp historyOrders) {
        this.orderRepository = orderRepository;
        this.openOrders = openOrders;
        this.historyOrders = historyOrders;
    }

    public Maybe<IOrder> getByID(final int orderID) {
        return orderInRepository(orderID)
            .switchIfEmpty(orderInOpenOrders(orderID))
            .switchIfEmpty(orderInHistory(orderID));
    }

    private Maybe<IOrder> orderInRepository(final int orderID) {
        return maybeOrder(orderRepository::getByID,
                          orderID,
                          "cache orders");
    }

    private Maybe<IOrder> orderInOpenOrders(final int orderID) {
        return maybeOrder(openOrders::getByID,
                          orderID,
                          "open orders");
    }

    private Maybe<IOrder> orderInHistory(final int orderID) {
        return maybeOrder(historyOrders::getByID,
                          orderID,
                          "history");
    }

    private Maybe<IOrder> maybeOrder(final Function<Integer, Maybe<IOrder>> orderLookup,
                                     final int orderID,
                                     final String lookupName) {
        return Maybe
            .defer(() -> orderLookup.apply(orderID))
            .doOnSubscribe(d -> logger.debug("Looking up orderID " + orderID + " in " + lookupName + "..."))
            .doAfterSuccess(order -> logger.debug("Found orderID " + orderID + " in " + lookupName + "."))
            .doOnComplete(() -> logger.error("OrderID " + orderID + " not found in " + lookupName + "!"));
    }
}
