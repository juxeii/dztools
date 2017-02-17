package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerTrade {

    private final OrderHandler orderHandler;
    private final StrategyUtil strategyUtil;

    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final OrderHandler orderHandler,
                       final StrategyUtil strategyUtil) {
        this.orderHandler = orderHandler;
        this.strategyUtil = strategyUtil;
    }

    public int handle(final int orderID,
                      final double orderParams[]) {
        logger.info("BrokerTrade handle called");
        if (!orderHandler.isOrderKnown(orderID)) {
            logger.info("BrokerTrade orderID " + orderID + " not found");
            return ReturnCodes.UNKNOWN_ORDER_ID;
        }

        final IOrder order = orderHandler.getOrder(orderID);
        if (order.getState() == IOrder.State.CLOSED)
            return ReturnCodes.ORDER_RECENTLY_CLOSED;

        fillOrderParams(order, orderParams);
        final int scaledAmount = orderHandler.scaleAmount(order.getAmount());
        logger.info("scaledAmount = " + scaledAmount);

        return scaledAmount;
    }

    private void fillOrderParams(final IOrder order,
                                 final double orderParams[]) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(order.getInstrument());
        final double pOpen = order.getOpenPrice();
        final double pClose = order.isLong()
                ? instrumentUtil.askQuote()
                : instrumentUtil.bidQuote();
        final double pRoll = 0.0; // Rollover currently not supported
        final double pProfit = order.getProfitLossInAccountCurrency();

        orderParams[0] = pOpen;
        orderParams[1] = pClose;
        orderParams[2] = pRoll;
        orderParams[3] = pProfit;

        logger.info("trade pOpen = " + pOpen
                + " pClose " + pClose
                + " pProfit " + pProfit);
    }
}
