package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.basic.SetLabelParams;

public class OrderSetLabel {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(OrderSetLabel.class);

    public OrderSetLabel(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public OrderSetLabelResult run(final IOrder order,
                                   final String newLabel) {
        final SetLabelParams setLabelParams = SetLabelParams
            .setLabelWith(order, newLabel)
            .doOnStart(() -> logger.info("Trying to set new label " + newLabel + " on order " + order.getLabel()))
            .doOnError(err -> logger.error("Failed to set new label " + newLabel + " on order "
                    + order.getLabel() + "! " + err.getMessage()))
            .doOnComplete(() -> logger.info("Setting new label " + newLabel
                    + " on order " + order.getLabel() + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        return runOnOrderUtil(setLabelParams);
    }

    private OrderSetLabelResult runOnOrderUtil(final SetLabelParams setLabelParams) {
        final Throwable resultError = tradeUtil
            .orderUtil()
            .paramsToObservable(setLabelParams)
            .ignoreElements()
            .blockingGet();

        return resultError == null
                ? OrderSetLabelResult.OK
                : OrderSetLabelResult.FAIL;
    }
}
