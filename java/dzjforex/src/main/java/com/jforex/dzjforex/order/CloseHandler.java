package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.Constant;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.Observable;

public class CloseHandler {

    private final TradeUtil tradeUtil;
    private final SetLabel setLabel;

    private final static Logger logger = LogManager.getLogger(CloseHandler.class);

    public CloseHandler(final TradeUtil tradeUtil,
                        final SetLabel setLabel) {
        this.tradeUtil = tradeUtil;
        this.setLabel = setLabel;
    }

    public int closeOrder(final IOrder order,
                          final double amount) {
        final CloseParams closeParams = CloseParams
            .withOrder(order)
            .closePartial(amount)
            .doOnStart(() -> logger.debug("Trying to close order with label "
                    + order.getLabel()
                    + " and amount " + amount))
            .doOnError(err -> ZorroLogger.logError("Failed to close order "
                    + order.getLabel() + "! "
                    + err.getMessage(), logger))
            .doOnComplete(() -> logger.debug("Closed order "
                    + order.getLabel() + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        final OrderEvent orderEvent = tradeUtil
            .orderUtil()
            .paramsToObservable(closeParams)
            .onErrorResumeNext(err -> {
                return Observable.just(new OrderEvent(null, OrderEventType.CLOSE_REJECTED, true));
            })
            .blockingLast();

        if (orderEvent.order() == null) {
            return Constant.ORDER_CLOSE_FAIL;
        }

        if (amount < order.getAmount()) {
            final String newLabel = tradeUtil
                .labelUtil()
                .create();
            final SetLabelResult setLabelResult = setLabel.run(order, newLabel);
            return setLabelResult == SetLabelResult.OK
                    ? Constant.ORDER_PARTIAL_CLOSE_OK
                    : Constant.ORDER_CLOSE_FAIL;
        }
        return Constant.ORDER_CLOSE_OK;
    }
}
