package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class BrokerSell {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerSell.class);

    public BrokerSell(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public int closeTrade(final BrokerSellData brokerSellData) {
        final Optional<IOrder> maybeOrderById = tradeUtil.maybeOrderForTrading(brokerSellData.nTradeID());
        return maybeOrderById.isPresent()
                ? closeTradeForValidOrder(brokerSellData, maybeOrderById.get())
                : ZorroReturnValues.BROKER_SELL_FAIL.getValue();
    }

    private int closeTradeForValidOrder(final BrokerSellData brokerSellData,
                                        final IOrder order) {
        logger.info("Trying to close trade for nTradeID " + brokerSellData.nTradeID()
                + " and nAmount " + brokerSellData.nAmount());
        final double amountToClose = tradeUtil.contractsToAmount(brokerSellData.nAmount());
        final CloseParams closeParams = tradeUtil
            .taskParams()
            .forClose(order, amountToClose);
        final OrderActionResult closeResult = tradeUtil.runTaskParams(closeParams);

        return closeResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : tradeUtil
                    .labelUtil()
                    .idFromOrder(order);
    }
}
