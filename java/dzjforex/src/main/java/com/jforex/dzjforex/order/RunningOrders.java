package com.jforex.dzjforex.order;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;

import io.reactivex.Observable;

public class RunningOrders {

    private final IEngine engine;

    private final static Logger logger = LogManager.getLogger(RunningOrders.class);

    public RunningOrders(final IEngine engine) {
        this.engine = engine;
    }

    public List<IOrder> get() {
        return Observable
            .fromCallable(() -> engine.getOrders())
            .doOnSubscribe(d -> logger.debug("Fetching running orders..."))
            .onErrorResumeNext(err -> {
                logger.error("Error while fetching running orders!" + err.getMessage());
                return Observable.just(new ArrayList<>());
            })
            .flatMap(Observable::fromIterable)
            .toList()
            .doOnSuccess(list -> logger.debug("Fetched " + list.size() + " runnings orders."))
            .blockingGet();
    }
}
