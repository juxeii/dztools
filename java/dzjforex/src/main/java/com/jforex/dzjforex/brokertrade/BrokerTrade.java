package com.jforex.dzjforex.brokertrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.math.CalculationUtil;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class BrokerTrade {

    private final TradeUtility tradeUtility;
    private final CalculationUtil calculationUtil;

    private final static double rollOverNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerTrade.class);

    public BrokerTrade(final TradeUtility tradeUtility,
                       final CalculationUtil calculationUtil) {
        this.tradeUtility = tradeUtility;
        this.calculationUtil = calculationUtil;
    }

    public Single<Integer> fillParams(final BrokerTradeData brokerTradeData) {
        return Maybe
            .defer(() -> tradeUtility.orderByID(brokerTradeData.orderID()))
            .toSingle()
            .doOnSuccess(order -> fillTradeParams(order, brokerTradeData))
            .map(order -> order.getState() == IOrder.State.CLOSED
                    ? ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue()
                    : tradeUtility.amountToContracts(order.getAmount()))
            .onErrorReturnItem(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue());
    }

    private void fillTradeParams(final IOrder order,
                                 final BrokerTradeData brokerTradeData) {
        final Instrument instrument = order.getInstrument();
        final double pOpen = order.getOpenPrice();
        final double pClose = calculationUtil.currentQuoteForOrderCommand(instrument, order.getOrderCommand());
        final double pRoll = rollOverNotSupported;
        final double pProfit = order.getProfitLossInAccountCurrency();

        brokerTradeData.fill(pOpen,
                             pClose,
                             pRoll,
                             pProfit);
        logger.trace("Trade params for orderID " + brokerTradeData.orderID() + ":\n"
                + "pOpen: " + pOpen + "\n"
                + "pClose: " + pClose + "\n"
                + "pRoll: " + pRoll + "\n"
                + "pProfit: " + pProfit);
    }
}
