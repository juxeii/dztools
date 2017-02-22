package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderClose;
import com.jforex.dzjforex.order.OrderCloseResult;
import com.jforex.dzjforex.order.OrderSetLabel;
import com.jforex.dzjforex.order.OrderSetLabelResult;
import com.jforex.dzjforex.order.TradeUtil;

public class BrokerSell {

    private final TradeUtil tradeUtil;
    private final OrderClose orderClose;
    private final OrderSetLabel orderSetLabel;

    private final static Logger logger = LogManager.getLogger(BrokerSell.class);

    public BrokerSell(final TradeUtil tradeUtil,
                      final OrderClose orderClose,
                      final OrderSetLabel orderSetLabel) {
        this.tradeUtil = tradeUtil;
        this.orderClose = orderClose;
        this.orderSetLabel = orderSetLabel;
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

        return amountToClose < order.getAmount()
                ? setNewLabel(order)
                : tradeUtil
                    .labelUtil()
                    .idFromOrder(order);
    }

    private int setNewLabel(final IOrder order) {
        final String newLabel = tradeUtil
            .labelUtil()
            .create();
        final OrderSetLabelResult setLabelResult = orderSetLabel.run(order, newLabel);
        if (setLabelResult == OrderSetLabelResult.OK) {
            final int newOrderID = tradeUtil
                .labelUtil()
                .idFromOrder(order);
            return newOrderID;
        }
        return ZorroReturnValues.BROKER_SELL_FAIL.getValue();
    }
}
