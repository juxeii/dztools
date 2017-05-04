package com.jforex.dzjforex.brokersell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.Single;

public class OrderClose {

    private final TradeUtility tradeUtility;
    private final OrderLabelUtil orderLabelUtil;

    private final static Logger logger = LogManager.getLogger(OrderClose.class);

    public OrderClose(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;

        orderLabelUtil = tradeUtility.orderLabelUtil();
    }

    public int run(final CloseParams closeParams) {
        final int orderID = orderLabelUtil.idFromOrder(closeParams.order());
        final double amountToClose = closeParams.partialCloseAmount();

        return Single
            .just(closeParams)
            .doOnSubscribe(d -> logger.info("Trying to close order with ID " + orderID
                    + " and amountToClose " + amountToClose))
            .map(tradeUtility::runTaskParams)
            .map(closeResult -> closeResult == OrderActionResult.FAIL
                    ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                    : orderID)
            .blockingGet();
    }
}
