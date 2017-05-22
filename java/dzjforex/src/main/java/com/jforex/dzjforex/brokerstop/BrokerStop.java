package com.jforex.dzjforex.brokerstop;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class BrokerStop {

    private final SetSLParamsRunner setSLParamsRunner;
    private final TradeUtility tradeUtility;

    public BrokerStop(final SetSLParamsRunner setSLParamsRunner,
                      final TradeUtility tradeUtility) {
        this.setSLParamsRunner = setSLParamsRunner;
        this.tradeUtility = tradeUtility;
    }

    public Single<Integer> setSL(final BrokerStopData brokerStopData) {
        return tradeUtility
            .orderForTrading(brokerStopData.nTradeID())
            .flatMapCompletable(order -> setSLParamsRunner.get(order, brokerStopData))
            .toSingleDefault(ZorroReturnValues.ADJUST_SL_OK.getValue())
            .onErrorReturnItem(ZorroReturnValues.ADJUST_SL_FAIL.getValue());
    }
}
