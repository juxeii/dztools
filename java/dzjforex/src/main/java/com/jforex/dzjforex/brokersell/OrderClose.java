package com.jforex.dzjforex.brokersell;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class OrderClose {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;

    public OrderClose(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;

        taskParams = tradeUtility.taskParams();
    }

    public Single<Integer> run(final IOrder order,
                               final BrokerSellData brokerSellData) {
        return Single
            .just(taskParams.forClose(order, tradeUtility.contractsToAmount(brokerSellData.nAmount())))
            .map(tradeUtility::runTaskParams)
            .map(closeResult -> evalCloseResult(closeResult, brokerSellData.nTradeID()));
    }

    private int evalCloseResult(final OrderActionResult closeResult,
                                final int orderID) {
        return closeResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : orderID;
    }
}
