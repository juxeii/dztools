package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.basic.SetSLParams;

public class SetSLHandler {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(SetSLHandler.class);

    public SetSLHandler(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public void setSL(final IOrder order,
                      final double slPrice) {
        logger.info("Trying to set stop loss " + slPrice
                + " on order with label " + order.getLabel());

        final SetSLParams setSLParams = SetSLParams
            .setSLAtPrice(order, slPrice)
            .build();

        tradeUtil
            .orderUtil()
            .paramsToObservable(setSLParams)
            .blockingLast();
    }
}
