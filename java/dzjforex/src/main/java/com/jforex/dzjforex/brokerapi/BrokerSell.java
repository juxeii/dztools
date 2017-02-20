package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.ZorroLogger;
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
            return Constant.BROKER_SELL_FAIL;
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
        if (amountToClose < order.getAmount()) {
            final String errorMsg = "Partical close not supported! Data:\n"
                    + "nTradeID: " + nTradeID + "\n"
                    + "nAmount: " + nAmount + "\n"
                    + "order amount: " + order.getAmount();
            ZorroLogger.logError(errorMsg, logger);
            return Constant.BROKER_SELL_FAIL;
        }

        closeHandler.closeOrder(order);
        if (!OrderStaticUtil.isClosed.test(order))
            return Constant.BROKER_SELL_FAIL;

        logger.info("Order with label " + order.getLabel() + " closed. Returning old nTradeID " + nTradeID);
        return nTradeID;
    }
}
