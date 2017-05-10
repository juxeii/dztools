package com.jforex.dzjforex.brokerstop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import io.reactivex.Single;

public class OrderSetSLParams {

    private final StopLoss stopLoss;
    private final RetryParams retryParams;

    private final static Logger logger = LogManager.getLogger(OrderSetSLParams.class);

    public OrderSetSLParams(final StopLoss stopLoss,
                            final RetryParams retryParams) {
        this.stopLoss = stopLoss;
        this.retryParams = retryParams;
    }

    public Single<SetSLParams> get(final IOrder order,
                                   final BrokerStopData brokerStopData) {
        return stopLoss
            .forPrice(order.getInstrument(), brokerStopData.slPrice())
            .map(slPrice -> create(order, slPrice));
    }

    public SetSLParams create(final IOrder order,
                              final double newSLPrice) {
        final String orderLabel = order.getLabel();
        final SetSLParams setSLParams = SetSLParams
            .setSLAtPrice(order, newSLPrice)
            .doOnStart(() -> logger.info("Trying to set new stop loss " + newSLPrice
                    + " on order " + orderLabel))
            .doOnError(e -> logger.error("Failed to set new stop loss " + newSLPrice
                    + " on order " + orderLabel
                    + "!" + e.getMessage()))
            .doOnComplete(() -> logger.info("Setting new Stop loss " + newSLPrice
                    + " on order " + orderLabel
                    + " done."))
            .retryOnReject(retryParams)
            .build();

        return setSLParams;
    }
}
