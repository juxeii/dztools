package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.order.CloseHandler;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.order.OrderStaticUtil;

public class BrokerSell {

    private final CloseHandler closeHandler;
    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerSell.class);

    public BrokerSell(final CloseHandler closeHandler,
                      final TradeUtil tradeUtil) {
        this.closeHandler = closeHandler;
        this.tradeUtil = tradeUtil;
    }

    public int closeTrade(final int nTradeID,
                          final int nAmount) {
        if (!tradeUtil.isOrderIDKnown(nTradeID)) {
            logger.error("Cannot close trade with unknown ID " + nTradeID);
            return Constant.UNKNOWN_ORDER_ID;
        }
        if (!tradeUtil.isTradingAllowed())
            return Constant.BROKER_SELL_FAIL;

        logger.info("Trying to close trade for order ID " + nTradeID
                + " and nAmount " + nAmount);
        return closeTradeForValidOrderID(nTradeID, nAmount);
    }

    private int closeTradeForValidOrderID(final int nTradeID,
                                          final double nAmount) {
        final IOrder order = tradeUtil.getOrder(nTradeID);
        final double amountToClose = tradeUtil.contractsToAmount(nAmount);
        if (!closeHandler.closeOrder(order, amountToClose)) {
            return Constant.BROKER_SELL_FAIL;
        }

        if (!OrderStaticUtil.isClosed.test(order)) {
            final int newOrderID = tradeUtil.createOrderID();
            tradeUtil.storeOrder(newOrderID, order);
            logger.info("Order " + nTradeID + " was partially closed. New ID is " + newOrderID);
            return newOrderID;
        } else {
            logger.info("Order " + nTradeID + " closed. Returning old ID " + nTradeID);
            return nTradeID;
        }
    }
}
