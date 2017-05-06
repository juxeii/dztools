package com.jforex.dzjforex.brokerstop;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerStop {

    private final OrderSetSL orderSetSL;
    private final TradeUtility tradeUtility;

    public BrokerStop(final OrderSetSL orderSetSL,
                      final TradeUtility tradeUtility) {
        this.orderSetSL = orderSetSL;
        this.tradeUtility = tradeUtility;
    }

    public int setSL(final BrokerStopData brokerStopData) {
        return tradeUtility
            .maybeOrderForTrading(brokerStopData.nTradeID())
            .map(order -> orderSetSL.run(order, brokerStopData))
            .defaultIfEmpty(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }
}
