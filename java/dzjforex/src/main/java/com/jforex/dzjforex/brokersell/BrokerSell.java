package com.jforex.dzjforex.brokersell;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerSell {

    private final TaskParamsRunner taskParamsRunner;
    private final TradeUtility tradeUtility;

    public BrokerSell(final TaskParamsRunner taskParamsRunner,
                      final TradeUtility tradeUtility) {
        this.taskParamsRunner = taskParamsRunner;
        this.tradeUtility = tradeUtility;
    }

    public int closeTrade(final BrokerSellData brokerSellData) {
        final int orderID = brokerSellData.nTradeID();
        return tradeUtility
            .orderForTrading(orderID)
            .map(order -> taskParamsRunner.startClose(order, brokerSellData))
            .map(closeResult -> evalCloseResult(closeResult, orderID))
            .onErrorReturnItem(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }

    private int evalCloseResult(final OrderActionResult closeResult,
                                final int orderID) {
        return closeResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : orderID;
    }
}
