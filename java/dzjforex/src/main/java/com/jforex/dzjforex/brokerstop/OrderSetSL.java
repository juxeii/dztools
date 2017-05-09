package com.jforex.dzjforex.brokerstop;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;

public class OrderSetSL {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;
    private final StopLoss stopLoss;

    public OrderSetSL(final TradeUtility tradeUtility,
                      final StopLoss stopLoss) {
        this.tradeUtility = tradeUtility;
        this.stopLoss = stopLoss;

        taskParams = tradeUtility.taskParams();
    }

    public int run(final IOrder order,
                   final BrokerStopData brokerStopData) {
        final double sl = brokerStopData.slPrice();

        return stopLoss.forPrice(order.getInstrument(), sl)
            .map(slPrice -> taskParams.forSetSL(order, slPrice))
            .map(tradeUtility::runTaskParams)
            .map(this::evalSLResult)
            .onErrorReturnItem(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }

    private int evalSLResult(final OrderActionResult setSLResult) {
        return setSLResult == OrderActionResult.FAIL
                ? ZorroReturnValues.ADJUST_SL_FAIL.getValue()
                : ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
