package com.jforex.dzjforex.brokertrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerTrade {

    private final TradeUtility tradeUtility;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;
    }

    public int orderInfo(final BrokerTradeData brokerTradeData) {
        return tradeUtility
            .orderByID(brokerTradeData.nTradeID())
            .map(order -> handleForOrder(order, brokerTradeData))
            .onErrorReturnItem(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue())
            .blockingGet();
    }

    private int handleForOrder(final IOrder order,
                               final BrokerTradeData brokerTradeData) {
        fillTradeParams(order, brokerTradeData);
        if (order.getState() == IOrder.State.CLOSED)
            return ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue();

        final int noOfContracts = tradeUtility.amountToContracts(order.getAmount());
        return noOfContracts;
    }

    private void fillTradeParams(final IOrder order,
                                 final BrokerTradeData brokerTradeData) {
        final Instrument instrument = order.getInstrument();
        final double pOpen = order.getOpenPrice();
        final double pClose = order.isLong()
                ? tradeUtility.currentAsk(instrument)
                : tradeUtility.currentBid(instrument);
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
