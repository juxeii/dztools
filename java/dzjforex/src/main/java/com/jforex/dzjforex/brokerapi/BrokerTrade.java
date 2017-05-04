package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerTrade {

    private final TradeUtility tradeUtil;
    private final StrategyUtil strategyUtil;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final TradeUtility tradeUtil) {
        this.tradeUtil = tradeUtil;
        strategyUtil = tradeUtil.strategyUtil();
    }

    public int orderInfo(final BrokerTradeData brokerTradeData) {
        return tradeUtil
            .maybeOrderByID(brokerTradeData.nTradeID())
            .map(order -> handleForOrder(order, brokerTradeData))
            .defaultIfEmpty(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue())
            .blockingGet();
    }

    private int handleForOrder(final IOrder order,
                               final BrokerTradeData brokerTradeData) {
        fillTradeParams(order, brokerTradeData);
        if (order.getState() == IOrder.State.CLOSED)
            return ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue();

        final int noOfContracts = tradeUtil.amountToContracts(order.getAmount());
        return noOfContracts;
    }

    private void fillTradeParams(final IOrder order,
                                 final BrokerTradeData brokerTradeData) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(order.getInstrument());
        final double pOpen = order.getOpenPrice();
        final double pClose = order.isLong()
                ? instrumentUtil.askQuote()
                : instrumentUtil.bidQuote();
        final double pRoll = rollOverNotSupported;
        final double pProfit = order.getProfitLossInAccountCurrency();
        brokerTradeData.fill(pOpen,
                             pClose,
                             pRoll,
                             pProfit);

        logger.trace("Trade params for nTradeID " + brokerTradeData.nTradeID() + "\n"
                + "pOpen: " + pOpen + "\n"
                + "pClose: " + pClose + "\n"
                + "pRoll: " + pRoll + "\n"
                + "pProfit: " + pProfit + "\n");
    }
}
