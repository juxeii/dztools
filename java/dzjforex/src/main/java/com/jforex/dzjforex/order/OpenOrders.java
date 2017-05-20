package com.jforex.dzjforex.order;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;

import io.reactivex.Single;

public class OpenOrders {

    private final IEngine engine;
    private final OrderRepository orderRepository;

    private final static Logger logger = LogManager.getLogger(OpenOrders.class);

    public OpenOrders(final IEngine engine,
                      final OrderRepository orderRepository) {
        this.engine = engine;
        this.orderRepository = orderRepository;
    }

    public Single<IOrder> getByID(final int orderID) {
        return ordersFromEngine()
            .doOnSuccess(orderRepository::store)
            .flatMap(orders -> orderRepository.getByID(orderID));
    }

    private Single<List<IOrder>> ordersFromEngine() {
        return Single
            .fromCallable(() -> engine.getOrders())
            .doOnSubscribe(d -> logger.debug("Fetching open orders..."))
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size() + " open orders."))
            .doOnError(e -> logger.error("Error while fetching open orders! " + e.getMessage()));
    }
}
