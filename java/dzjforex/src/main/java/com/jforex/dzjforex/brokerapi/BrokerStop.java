package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerStop {

    private final OrderUtil orderUtil;
    private final OrderHandler orderHandler;
    private final AccountInfo accountInfo;

    private final static Logger logger = LogManager.getLogger(BrokerStop.class);

    public BrokerStop(final StrategyUtil strategyUtil,
                      final OrderHandler orderHandler,
                      final AccountInfo accountInfo) {
        this.orderHandler = orderHandler;
        this.accountInfo = accountInfo;

        orderUtil = strategyUtil.orderUtil();
    }

    public int setSL(final int orderID,
                     final double newSLPrice) {
        logger.info("setSL called with newSLPrice " + newSLPrice);
        if (!accountInfo.isTradingAllowed() || !orderHandler.isOrderKnown(orderID))
            return Constant.UNKNOWN_ORDER_ID;

        final IOrder order = orderHandler.getOrder(orderID);
        return setSLPrice(order, newSLPrice);
    }

    private int setSLPrice(final IOrder order,
                           final double newSLPrice) {
        logger.info("setSL internal called with newSLPrice " + newSLPrice);
        final double roundedSLPrice = MathUtil.roundPrice(newSLPrice, order.getInstrument());
        final SetSLParams setSLParams = SetSLParams
            .setSLAtPrice(order, roundedSLPrice)
            .build();

        orderUtil
            .paramsToObservable(setSLParams)
            .blockingLast();

        return Constant.ADJUST_SL_OK;
    }
}
