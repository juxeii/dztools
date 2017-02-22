package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerTrade {

    private final TradeUtil tradeUtil;
    private final OrderRepository orderRepository;
    private final StrategyUtil strategyUtil;

    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
        orderRepository = tradeUtil.orderRepository();
        strategyUtil = tradeUtil.strategyUtil();
    }

    public int fillTradeParams(final int orderID,
                               final double orderParams[]) {
        final IOrder order = orderRepository.orderByID(orderID);
        if (order == null) {
            logger.error("BrokerTrade orderID " + orderID + " not found");
            return ZorroReturnValues.UNKNOWN_ORDER_ID.getValue();
        }

        fillOrderParams(order, orderParams);
        if (order.getState() == IOrder.State.CLOSED) {
            logger.info("Order with ID " + orderID + " was recently closed.");
            return ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue();
        }
        final int noOfContracts = tradeUtil.scaleAmount(order.getAmount());

        logger.trace("Trade params for orderID " + orderID + "\n"
                + "pOpen: " + orderParams[0] + "\n"
                + "pClose: " + orderParams[1] + "\n"
                + "pRoll: " + orderParams[2] + "\n"
                + "pProfit: " + orderParams[3] + "\n"
                + "noOfContracts: " + noOfContracts);
        return noOfContracts;
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
    }
}
