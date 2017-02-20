package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.Observable;

public class CloseHandler {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(CloseHandler.class);

    public CloseHandler(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public void closeOrder(final IOrder order) {
        final CloseParams closeParams = CloseParams
            .withOrder(order)
            .doOnStart(() -> logger.info("Trying to close order with label " + order.getLabel()))
            .doOnError(err -> ZorroLogger.logError("Failed to close trade! " + err.getMessage(), logger))
            .build();

        tradeUtil
            .orderUtil()
            .paramsToObservable(closeParams)
            .onErrorResumeNext(err -> {
                return Observable.just(new OrderEvent(null, OrderEventType.CLOSE_REJECTED, true));
            })
            .blockingLast();
    }
}
