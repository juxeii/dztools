package com.jforex.dzjforex.order;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;

import io.reactivex.Single;

public class OpenOrders {

    private final IEngine engine;

    private final static Logger logger = LogManager.getLogger(OpenOrders.class);

    public OpenOrders(final IEngine engine) {
        this.engine = engine;
    }

    public Single<List<IOrder>> get() {
        return Single
            .fromCallable(() -> engine.getOrders())
            .doOnSubscribe(d -> logger.debug("Fetching open orders..."))
            .doOnSuccess(list -> logger.debug("Fetched " + list.size() + " open orders."))
            .doOnError(err -> logger.error("Error while fetching open orders!" + err.getMessage()));
    }
}
