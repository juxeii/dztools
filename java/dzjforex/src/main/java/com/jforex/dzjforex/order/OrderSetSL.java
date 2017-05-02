package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.basic.SetSLParams;

public class OrderSetSL {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(OrderSetSL.class);

    public OrderSetSL(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public OrderActionResult run(final IOrder order,
                                 final double newSLPrice) {
        final SetSLParams setSLParams = SetSLParams
            .setSLAtPrice(order, newSLPrice)
            .doOnStart(() -> logger.info("Trying to set new stop loss " + newSLPrice
                    + " on order " + order.getLabel()))
            .doOnError(err -> logger.error("Failed to set new stop loss " + newSLPrice
                    + " on order " + order.getLabel() + "!" + err.getMessage()))
            .doOnComplete(() -> logger.info("Setting new Stop loss " + newSLPrice
                    + " on order " + order.getLabel() + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        return tradeUtil.runTaskParams(setSLParams);
    }
}
