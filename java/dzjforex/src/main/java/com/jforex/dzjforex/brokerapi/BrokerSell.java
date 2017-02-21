package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.order.CloseHandler;
import com.jforex.dzjforex.order.TradeUtil;

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
        if (!tradeUtil.isTradingAllowed())
            return Constant.BROKER_SELL_FAIL;
        final IOrder order = tradeUtil.orderByID(nTradeID);
        if (order == null)
            return Constant.BROKER_SELL_FAIL;

        logger.info("Trying to close trade for nTradeID " + nTradeID
                + " and nAmount " + nAmount);
        return closeTradeForValidOrderID(nTradeID, nAmount);
    }

    private int closeTradeForValidOrderID(final int nTradeID,
                                          final double nAmount) {
        final IOrder order = tradeUtil.getOrder(nTradeID);
        final double amountToClose = tradeUtil.contractsToAmount(nAmount);

        final int closeResult = closeHandler.closeOrder(order, amountToClose);
        if (closeResult == Constant.ORDER_CLOSE_OK)
            return nTradeID;
        if (closeResult == Constant.ORDER_CLOSE_FAIL)
            return Constant.BROKER_SELL_FAIL;

        final int newOrderID = tradeUtil
            .labelUtil()
            .orderId(order);
        tradeUtil.storeOrder(newOrderID, order);
        return newOrderID;
    }
}
