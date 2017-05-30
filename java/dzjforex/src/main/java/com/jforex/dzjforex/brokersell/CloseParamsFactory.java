package com.jforex.dzjforex.brokersell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.Single;

public class CloseParamsFactory {

    private final RetryParams retryParams;

    private final static Logger logger = LogManager.getLogger(CloseParamsFactory.class);

    public CloseParamsFactory(final RetryParams retryParams) {
        this.retryParams = retryParams;
    }

    public Single<CloseParams> get(final IOrder order,
                                   final BrokerSellData brokerSellData) {
        return Single.fromCallable(() -> create(order,
                                                brokerSellData.orderID(),
                                                brokerSellData.amount()));
    }

    private CloseParams create(final IOrder order,
                               final int orderID,
                               final double closeAmount) {
        return CloseParams
            .withOrder(order)
            .closePartial(closeAmount)
            .doOnStart(() -> logger.info("Trying to close orderID " + orderID
                    + " with amount " + closeAmount))
            .doOnError(e -> logger.error("Failed to close orderID " + orderID
                    + "! " + e.getMessage()))
            .doOnComplete(() -> logger.info("Closing orderID " + orderID + " done."))
            .retryOnReject(retryParams)
            .build();
    }
}
