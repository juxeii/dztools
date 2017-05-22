package com.jforex.dzjforex.brokersell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.Single;

public class OrderCloseParams {

    private final TradeUtility tradeUtility;

    private final static Logger logger = LogManager.getLogger(OrderCloseParams.class);

    public OrderCloseParams(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;
    }

    public Single<CloseParams> get(final IOrder order,
                                   final BrokerSellData brokerSellData) {
        final double closeAmount = brokerSellData.amount();
        final String orderLabel = order.getLabel();
        final CloseParams closeParams = CloseParams
            .withOrder(order)
            .closePartial(closeAmount)
            .doOnStart(() -> logger.info("Trying to close order " + orderLabel
                    + " with amount " + closeAmount))
            .doOnError(e -> logger.error("Failed to close order " + orderLabel
                    + "! " + e.getMessage()))
            .doOnComplete(() -> logger.info("Closing order " + orderLabel + " done."))
            .retryOnReject(tradeUtility.retryParams())
            .build();

        return Single.just(closeParams);
    }
}
