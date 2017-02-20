package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerTrade {

    private final OrderRepository orderHandler;
    private final StrategyUtil strategyUtil;

    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final OrderRepository orderHandler,
                       final StrategyUtil strategyUtil) {
        this.orderHandler = orderHandler;
        this.strategyUtil = strategyUtil;
    }

    public int fillTradeParams(final int orderID,
                               final double orderParams[]) {
        logger.info("BrokerTrade fillTradeParams called for id " + orderID);
        if (!orderHandler.isOrderKnown(orderID)) {
            logger.info("BrokerTrade orderID " + orderID + " not found");
            return Constant.UNKNOWN_ORDER_ID;
        }

        final IOrder order = orderHandler.getOrder(orderID);
        fillOrderParams(order, orderParams);
        if (order.getState() == IOrder.State.CLOSED) {
            logger.info("BrokerTrade recently closed!");
            return Constant.ORDER_RECENTLY_CLOSED;
        }
        final int noOfContracts = orderHandler.scaleAmount(order.getAmount());
        logger.info("noOfContracts = " + noOfContracts);

        return noOfContracts;
    }

    private void fillOrderParams(final IOrder order,
                                 final double orderParams[]) {
        logger.info("BrokerTrade fillOrderParams called");
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
