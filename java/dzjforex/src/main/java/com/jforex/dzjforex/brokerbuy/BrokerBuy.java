package com.jforex.dzjforex.brokerbuy;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerBuy {

    private final OrderSubmit orderSubmit;
    private final TradeUtility tradeUtility;

    public BrokerBuy(final OrderSubmit orderSubmit,
                     final TradeUtility tradeUtility) {
        this.orderSubmit = orderSubmit;
        this.tradeUtility = tradeUtility;
    }

    public int openTrade(final BrokerBuyData brokerBuyData) {
        return tradeUtility
            .maybeInstrumentForTrading(brokerBuyData.instrumentName())
            .map(instrument -> orderSubmit.run(instrument, brokerBuyData))
            .defaultIfEmpty(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }
}
