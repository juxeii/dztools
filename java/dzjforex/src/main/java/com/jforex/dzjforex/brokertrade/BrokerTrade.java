package com.jforex.dzjforex.brokertrade;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class BrokerTrade {

    private final TradeUtility tradeUtility;

    public BrokerTrade(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;
    }

    public Single<Integer> fillParams(final BrokerTradeData brokerTradeData) {
        return Maybe
            .defer(() -> tradeUtility.orderByID(brokerTradeData.orderID()))
            .toSingle()
            .doOnSuccess(brokerTradeData::fill)
            .map(order -> order.getState() == IOrder.State.CLOSED
                    ? ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue()
                    : tradeUtility.amountToContracts(order.getAmount()))
            .onErrorReturnItem(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue());
    }
}
