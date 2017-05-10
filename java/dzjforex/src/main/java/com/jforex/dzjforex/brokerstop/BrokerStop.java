package com.jforex.dzjforex.brokerstop;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.order.TradeUtility;

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
            .maybeOrderForTrading(brokerStopData.nTradeID())
            .map(order -> taskParamsRunner.startSetSL(order, brokerStopData))
            .map(this::evalSLResult)
            .defaultIfEmpty(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }

    private int evalSLResult(final OrderActionResult setSLResult) {
        return setSLResult == OrderActionResult.FAIL
                ? ZorroReturnValues.ADJUST_SL_FAIL.getValue()
                : ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
