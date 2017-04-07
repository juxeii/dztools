package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderClose;
import com.jforex.dzjforex.order.OrderCloseResult;
import com.jforex.dzjforex.order.TradeUtil;

public class BrokerSell {

    private final TradeUtil tradeUtil;
    private final OrderClose orderClose;

    private final static Logger logger = LogManager.getLogger(BrokerSell.class);

    public BrokerSell(final TradeUtil tradeUtil,
                      final OrderClose orderClose) {
        this.tradeUtil = tradeUtil;
        this.orderClose = orderClose;
    }

    public int closeTrade(final int nTradeID,
                          final int nAmount) {
        if (!tradeUtil.isTradingAllowed())
            return ZorroReturnValues.BROKER_SELL_FAIL.getValue();
        final IOrder order = tradeUtil.orderByID(nTradeID);
        if (order == null)
            return ZorroReturnValues.BROKER_SELL_FAIL.getValue();

        logger.info("Trying to close trade for nTradeID " + nTradeID
                + " and nAmount " + nAmount);
        return closeTradeForValidOrder(order, nAmount);
    }

    private int closeTradeForValidOrder(final IOrder order,
                                        final double nAmount) {
        final double amountToClose = tradeUtil.contractsToAmount(nAmount);

        final OrderCloseResult closeResult = orderClose.run(order, amountToClose);
        if (closeResult == OrderCloseResult.FAIL)
            return ZorroReturnValues.BROKER_SELL_FAIL.getValue();

        return closeResult == OrderCloseResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : tradeUtil
                    .labelUtil()
                    .idFromOrder(order);
    }
}
