package com.jforex.dzjforex.order;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;

import io.reactivex.Single;

public class OpenOrdersProvider {

    private final IEngine engine;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(OpenOrdersProvider.class);

    public OpenOrdersProvider(final IEngine engine,
                              final PluginConfig pluginConfig) {
        this.engine = engine;
        this.pluginConfig = pluginConfig;
    }

    public Single<List<IOrder>> get() {
        return Single
            .fromCallable(engine::getOrders)
            .doOnSubscribe(d -> logger.debug("Fetching open orders..."))
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size() + " open orders."))
            .doOnError(e -> logger.error("Fetching open orders failed! " + e.getMessage()))
            .retryWhen(RxUtility.retryForHistory(pluginConfig));
    }
}
