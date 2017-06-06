package com.jforex.dzjforex.brokertrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.misc.PriceProvider;

public class BrokerTradeData {

    private final int orderID;
    private final double tradeParams[];
    private final PriceProvider priceProvider;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTradeData.class);

    public BrokerTradeData(final int orderID,
                           final double tradeParams[],
                           final PriceProvider priceProvider) {
        this.orderID = orderID;
        this.tradeParams = tradeParams;
        this.priceProvider = priceProvider;
    }

    public int orderID() {
        return orderID;
    }

    public void fill(final IOrder order) {
        final Instrument instrument = order.getInstrument();
        final double pOpen = order.getOpenPrice();
        final double pClose = priceProvider.forOrder(order);
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
