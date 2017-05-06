package com.jforex.dzjforex.brokersell;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerSell {

    private final OrderClose orderClose;
    private final TradeUtility tradeUtility;

    public BrokerSell(final OrderClose orderClose,
                      final TradeUtility tradeUtility) {
        this.orderClose = orderClose;
        this.tradeUtility = tradeUtility;
    }

    public int closeTrade(final BrokerSellData brokerSellData) {
        return tradeUtility
            .maybeOrderForTrading(brokerSellData.nTradeID())
            .map(order -> orderClose.run(order, brokerSellData))
            .defaultIfEmpty(ZorroReturnValues.BROKER_SELL_FAIL.getValue())
            .blockingGet();
    }
}
