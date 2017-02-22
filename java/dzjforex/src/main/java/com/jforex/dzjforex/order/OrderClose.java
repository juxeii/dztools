package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class OrderClose {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(OrderClose.class);

    public OrderClose(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public OrderCloseResult run(final IOrder order,
                                final double closeAmount) {
        final CloseParams closeParams = CloseParams
            .withOrder(order)
            .closePartial(closeAmount)
            .doOnStart(() -> logger.info("Trying to close order " + order.getLabel()
                    + " with amount " + closeAmount))
            .doOnError(err -> logger.error("Failed to close order "
                    + order.getLabel() + "! " + err.getMessage()))
            .doOnComplete(() -> logger.info("Closing order " + order.getLabel() + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        return runOnOrderUtil(order, closeParams);
    }

    private OrderCloseResult runOnOrderUtil(final IOrder order,
                                            final CloseParams closeParams) {
        final Throwable resultError = tradeUtil
            .orderUtil()
            .paramsToObservable(closeParams)
            .ignoreElements()
            .blockingGet();

        if (resultError != null)
            return OrderCloseResult.FAIL;
        else
            return closeParams.partialCloseAmount() < order.getAmount()
                    ? OrderCloseResult.PARTIAL_OK
                    : OrderCloseResult.OK;
    }
}
