package com.jforex.dzjforex.brokerstop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import io.reactivex.Single;

public class SetSLParamsFactory {

    private final StopLoss stopLoss;
    private final RetryParams retryParams;

    private final static Logger logger = LogManager.getLogger(SetSLParamsFactory.class);

    public SetSLParamsFactory(final StopLoss stopLoss,
                              final RetryParams retryParams) {
        this.stopLoss = stopLoss;
        this.retryParams = retryParams;
    }

    public Single<SetSLParams> get(final IOrder order,
                                   final BrokerStopData brokerStopData) {
        return Single
            .defer(() -> stopLoss.forSetSL(order, brokerStopData.slPrice()))
            .map(slPrice -> create(order,
                                   brokerStopData.orderID(),
                                   slPrice));
    }

    private SetSLParams create(final IOrder order,
                               final int orderID,
                               final double newSLPrice) {
        return SetSLParams
            .setSLAtPrice(order, newSLPrice)
            .doOnStart(() -> logger.info("Trying to set new stop loss " + newSLPrice
                    + " on orderID " + orderID))
            .doOnError(e -> logger.error("Failed to set new stop loss " + newSLPrice
                    + " on orderID " + orderID
                    + "!" + e.getMessage()))
            .doOnComplete(() -> logger.info("Setting new Stop loss " + newSLPrice
                    + " on orderID " + orderID
                    + " done."))
            .retryOnReject(retryParams)
            .build();
    }
}
