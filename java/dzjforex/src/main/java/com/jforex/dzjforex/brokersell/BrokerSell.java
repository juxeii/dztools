package com.jforex.dzjforex.brokersell;

import com.jforex.dzjforex.config.ZorroReturnValues;
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
            .flatMapCompletable(order -> taskParamsRunner.startClose(order, brokerSellData))
            .toSingleDefault(orderID)
            .onErrorReturnItem(ZorroReturnValues.BROKER_SELL_FAIL.getValue())
            .blockingGet();
    }
}
