package com.jforex.dzjforex.brokerstop;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerStop {

    private final OrderSetSL orderSetSL;
    private final StopLoss stopLoss;
    private final TradeUtility tradeUtility;

    public BrokerStop(final OrderSetSL orderSetSL,
                      final StopLoss stopLoss,
                      final TradeUtility tradeUtility) {
        this.orderSetSL = orderSetSL;
        this.stopLoss = stopLoss;
        this.tradeUtility = tradeUtility;
    }

    public int setSL(final BrokerStopData brokerStopData) {
        return tradeUtility
            .maybeOrderForTrading(brokerStopData.nTradeID())
            .flatMapSingle(order -> {
                final double slPrice = stopLoss
                    .forPrice(order.getInstrument(), brokerStopData.slPrice())
                    .blockingGet();
                return orderSetSL.run(order, slPrice);
            })
            .onErrorReturnItem(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }
}
