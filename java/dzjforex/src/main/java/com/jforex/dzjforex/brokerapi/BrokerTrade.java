package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerTrade {

    private final TradeUtil tradeUtil;
    private final StrategyUtil strategyUtil;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
        strategyUtil = tradeUtil.strategyUtil();
    }

    public int fillTradeParams(final int nTradeID,
                               final double orderParams[]) {
        final IOrder order = tradeUtil.orderByID(nTradeID);
        if (order == null)
            return ZorroReturnValues.UNKNOWN_ORDER_ID.getValue();

        fillTradeParams(order, orderParams);
        if (order.getState() == IOrder.State.CLOSED) {
            logger.debug("Order with ID " + nTradeID + " was recently closed.");
            return ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue();
        }
        final int noOfContracts = tradeUtil.amountToContracts(order.getAmount());

        logger.trace("Trade params for nTradeID " + nTradeID + "\n"
                + "pOpen: " + orderParams[0] + "\n"
                + "pClose: " + orderParams[1] + "\n"
                + "pRoll: " + orderParams[2] + "\n"
                + "pProfit: " + orderParams[3] + "\n"
                + "noOfContracts: " + noOfContracts);
        return noOfContracts;
    }

    private void fillTradeParams(final IOrder order,
                                 final double orderParams[]) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(order.getInstrument());
        final double pOpen = order.getOpenPrice();
        final double pClose = order.isLong()
                ? instrumentUtil.askQuote()
                : instrumentUtil.bidQuote();
        final double pRoll = rollOverNotSupported;
        final double pProfit = order.getProfitLossInAccountCurrency();

        orderParams[0] = pOpen;
        orderParams[1] = pClose;
        orderParams[2] = pRoll;
        orderParams[3] = pProfit;
    }
}
