package com.jforex.dzjforex.brokerstop;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class OrderSetSL {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;

    public OrderSetSL(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;

        taskParams = tradeUtility.taskParams();
    }

    public Single<Integer> run(final IOrder order,
                               final double slPrice) {
        return Single
            .fromCallable(() -> taskParams.forSetSL(order, slPrice))
            .map(tradeUtility::runTaskParams)
            .map(this::evalSLResult);
    }

    private int evalSLResult(final OrderActionResult setSLResult) {
        return setSLResult == OrderActionResult.FAIL
                ? ZorroReturnValues.ADJUST_SL_FAIL.getValue()
                : ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
