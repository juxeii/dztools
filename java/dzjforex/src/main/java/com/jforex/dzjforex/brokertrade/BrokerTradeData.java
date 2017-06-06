package com.jforex.dzjforex.brokertrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.math.CalculationUtil;

public class BrokerTradeData {

    private final int orderID;
    private final double tradeParams[];
    private final CalculationUtil calculationUtil;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTradeData.class);

    public BrokerTradeData(final int orderID,
                           final double tradeParams[],
                           final CalculationUtil calculationUtil) {
        this.orderID = orderID;
        this.tradeParams = tradeParams;
        this.calculationUtil = calculationUtil;
    }

    public int orderID() {
        return orderID;
    }

    public void fill(final IOrder order) {
        final Instrument instrument = order.getInstrument();
        final double pOpen = order.getOpenPrice();
        final double pClose = calculationUtil.currentQuoteForOrderCommand(instrument, order.getOrderCommand());
        final double pRoll = rollOverNotSupported;
        final double pProfit = order.getProfitLossInAccountCurrency();

        tradeParams[0] = pOpen;
        tradeParams[1] = pClose;
        tradeParams[2] = pRoll;
        tradeParams[3] = pProfit;
        logger.trace("Trade params for order " + orderID + ":\n"
                + "pOpen: " + pOpen + "\n"
                + "pClose: " + pClose + "\n"
                + "pRoll: " + pRoll + "\n"
                + "pProfit: " + pProfit);
    }
}
