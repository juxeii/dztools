package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.programming.order.task.params.basic.SetLabelParams;

public class SetLabel {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(SetLabel.class);

    public SetLabel(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public SetLabelResult run(final IOrder order,
                              final String label) {
        final SetLabelParams setLabelParams = SetLabelParams
            .setLabelWith(order, label)
            .doOnStart(() -> logger.debug("Trying to set new label "
                    + label
                    + " for partial closed order " + order.getLabel()))
            .doOnError(err -> ZorroLogger.logError("Failed to set new label " + label + " for order "
                    + order.getLabel() + "! "
                    + err.getMessage(), logger))
            .doOnComplete(() -> logger.debug("Setting new label "
                    + label + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        final Throwable resultError = tradeUtil
            .orderUtil()
            .paramsToObservable(setLabelParams)
            .ignoreElements()
            .blockingGet();

        return resultError == null
                ? SetLabelResult.OK
                : SetLabelResult.FAIL;
    }
}
