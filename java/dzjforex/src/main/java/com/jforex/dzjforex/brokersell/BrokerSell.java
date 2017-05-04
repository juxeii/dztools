package com.jforex.dzjforex.brokersell;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class BrokerSell {

    private final OrderClose orderClose;
    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;

    public BrokerSell(final OrderClose orderClose,
                      final TradeUtility tradeUtility) {
        this.orderClose = orderClose;
        this.tradeUtility = tradeUtility;

        taskParams = tradeUtility.taskParams();
    }

    public int closeTrade(final BrokerSellData brokerSellData) {
        return tradeUtility
            .maybeOrderForTrading(brokerSellData.nTradeID())
            .map(order -> runClose(order, brokerSellData))
            .defaultIfEmpty(ZorroReturnValues.BROKER_SELL_FAIL.getValue())
            .blockingGet();
    }

    private int runClose(final IOrder order,
                         final BrokerSellData brokerSellData) {
        final double amountToClose = tradeUtility.contractsToAmount(brokerSellData.nAmount());
        final CloseParams closeParams = taskParams.forClose(order, amountToClose);
        return orderClose.run(closeParams);
    }
}
