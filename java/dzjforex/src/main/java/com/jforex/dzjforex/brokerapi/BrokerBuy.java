package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.SubmitParams;

public class BrokerBuy {

    private final TradeUtility tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerBuy.class);

    public BrokerBuy(final TradeUtility tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public int openTrade(final BrokerBuyData brokerBuyData) {
        return tradeUtil
            .maybeInstrumentForTrading(brokerBuyData.instrumentName())
            .map(instrument -> submit(instrument, brokerBuyData))
            .defaultIfEmpty(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }

    private int submit(final Instrument instrument,
                       final BrokerBuyData brokerBuyData) {
        logger.debug("Trying to open trade for " + instrument
                + " with nAmount: " + brokerBuyData.contracts()
                + " and dStopDist: " + brokerBuyData.stopDistance());
        final String label = tradeUtil
            .labelUtil()
            .create();
        final OrderActionResult submitResult = getSubmitResult(instrument,
                                                               label,
                                                               brokerBuyData);
        if (submitResult == OrderActionResult.FAIL)
            return ZorroReturnValues.BROKER_BUY_FAIL.getValue();

        final int orderID = tradeUtil
            .labelUtil()
            .idFromLabel(label);
        final IOrder order = tradeUtil
            .maybeOrderByID(orderID)
            .blockingGet();
        brokerBuyData.fillOpenPrice(order.getOpenPrice());
        final double dStopDist = brokerBuyData.stopDistance();

        return dStopDist == -1
                ? ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue()
                : orderID;
    }

    private OrderActionResult getSubmitResult(final Instrument instrument,
                                              final String label,
                                              final BrokerBuyData brokerBuyData) {
        final double contracts = brokerBuyData.contracts();
        final double dStopDist = brokerBuyData.stopDistance();

        final double amount = tradeUtil.contractsToAmount(contracts);
        final OrderCommand orderCommand = tradeUtil.orderCommandForContracts(contracts);
        final double slPrice = tradeUtil
            .stopLoss()
            .calculate(instrument,
                         orderCommand,
                         dStopDist);
        final SubmitParams submitParams = tradeUtil
            .taskParams()
            .forSubmit(instrument,
                       orderCommand,
                       amount,
                       label,
                       slPrice);
        return tradeUtil.runTaskParams(submitParams);
    }
}
