package com.jforex.dzjforex.brokersell;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class OrderClose {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;

    public OrderClose(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;

        taskParams = tradeUtility.taskParams();
    }

    public int run(final IOrder order,
                   final BrokerSellData brokerSellData) {
        final int orderID = brokerSellData.nTradeID();
        final double amountToClose = tradeUtility.contractsToAmount(brokerSellData.nAmount());

        return Single
            .just(taskParams.forClose(order, amountToClose))
            .map(tradeUtility::runTaskParams)
            .map(closeResult -> evalCloseResult(closeResult, orderID))
            .blockingGet();
    }

    private int evalCloseResult(final OrderActionResult closeResult,
                                final int orderID) {
        return closeResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : orderID;
    }
}
