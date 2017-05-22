package com.jforex.dzjforex.brokertrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class BrokerTrade {

    private final TradeUtility tradeUtility;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;
    }

    public Single<Integer> fillParams(final BrokerTradeData brokerTradeData) {
        return Single.defer(() -> tradeUtility
            .orderByID(brokerTradeData.nTradeID())
            .doOnSuccess(order -> fillTradeParams(order, brokerTradeData))
            .map(order -> order.getState() == IOrder.State.CLOSED
                    ? ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue()
                    : tradeUtility.amountToContracts(order.getAmount()))
            .onErrorReturnItem(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue()));
    }

    private void fillTradeParams(final IOrder order,
                                 final BrokerTradeData brokerTradeData) {
        final Instrument instrument = order.getInstrument();
        final double pOpen = order.getOpenPrice();
        final double pClose = order.isLong()
                ? tradeUtility.ask(instrument)
                : tradeUtility.bid(instrument);
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
