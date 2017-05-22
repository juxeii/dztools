package com.jforex.dzjforex.brokersell;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerSell {

    private final CloseParamsRunner closeParamsRunner;
    private final TradeUtility tradeUtility;

    public BrokerSell(final CloseParamsRunner closeParamsRunner,
                      final TradeUtility tradeUtility) {
        this.closeParamsRunner = closeParamsRunner;
        this.tradeUtility = tradeUtility;
    }

    public int closeTrade(final BrokerSellData brokerSellData) {
        final int orderID = brokerSellData.nTradeID();
        return tradeUtility
            .orderForTrading(orderID)
            .flatMapCompletable(order -> closeParamsRunner.get(order, brokerSellData))
            .toSingleDefault(orderID)
            .onErrorReturnItem(ZorroReturnValues.BROKER_SELL_FAIL.getValue())
            .blockingGet();
    }
}
