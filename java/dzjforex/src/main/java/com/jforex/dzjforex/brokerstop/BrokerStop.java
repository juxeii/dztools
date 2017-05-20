package com.jforex.dzjforex.brokerstop;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class BrokerStop {

    private final TaskParamsRunner taskParamsRunner;
    private final TradeUtility tradeUtility;

    public BrokerStop(final TaskParamsRunner taskParamsRunner,
                      final TradeUtility tradeUtility) {
        this.taskParamsRunner = taskParamsRunner;
        this.tradeUtility = tradeUtility;
    }

    public int setSL(final BrokerStopData brokerStopData) {
        return tradeUtility
            .orderForTrading(brokerStopData.nTradeID())
            .flatMapCompletable(order -> taskParamsRunner.startSetSL(order, brokerStopData))
            .andThen(Single.just(ZorroReturnValues.ADJUST_SL_OK.getValue()))
            .onErrorReturnItem(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }
}
