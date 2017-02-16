package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerTrade {

    private final OrderHandler orderHandler;
    private final StrategyUtil strategyUtil;

    public BrokerTrade(final OrderHandler orderHandler,
                       final StrategyUtil strategyUtil) {
        this.orderHandler = orderHandler;
        this.strategyUtil = strategyUtil;
    }

    public int handle(final int orderID,
                      final double orderParams[]) {
        if (!orderHandler.isOrderKnown(orderID))
            return ReturnCodes.UNKNOWN_ORDER_ID;

        final IOrder order = orderHandler.getOrder(orderID);
        if (order.getState() == IOrder.State.CLOSED)
            return ReturnCodes.ORDER_RECENTLY_CLOSED;

        fillOrderParams(order, orderParams);
        return orderHandler.scaleAmount(order.getAmount());
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
