package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderSubmit;
import com.jforex.dzjforex.order.TradeUtil;

public class BrokerBuy {

    private final OrderSubmit orderSubmit;
    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerBuy.class);

    public BrokerBuy(final OrderSubmit orderSubmit,
                     final TradeUtil tradeUtil) {
        this.orderSubmit = orderSubmit;
        this.tradeUtil = tradeUtil;
    }

    public int openTrade(final String instrumentName,
                         final double tradeParams[]) {
        final Optional<Instrument> maybeInstrument = tradeUtil.maybeInstrumentForTrading(instrumentName);
        if (!maybeInstrument.isPresent())
            return ZorroReturnValues.BROKER_BUY_FAIL.getValue();

        logger.info("Trying to open trade for " + instrumentName
                + " with nAmount: " + tradeParams[0]
                + " and dStopDist: " + tradeParams[1]);
        return submit(maybeInstrument.get(), tradeParams);
    }

    private int submit(final Instrument instrument,
                       final double tradeParams[]) {
        final String label = tradeUtil
            .labelUtil()
            .create();
        final OrderActionResult submitResult = getSubmitResult(instrument,
                                                               label,
                                                               tradeParams);
        if (submitResult == OrderActionResult.FAIL)
            return ZorroReturnValues.BROKER_BUY_FAIL.getValue();

        final int orderID = tradeUtil
            .labelUtil()
            .idFromLabel(label);
        final IOrder order = tradeUtil.orderByID(orderID);
        tradeParams[2] = order.getOpenPrice();
        final double dStopDist = tradeParams[1];

        return dStopDist == -1
                ? ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue()
                : orderID;
    }

    private OrderActionResult getSubmitResult(final Instrument instrument,
                                              final String label,
                                              final double tradeParams[]) {
        final double contracts = tradeParams[0];
        final double dStopDist = tradeParams[1];

        final double amount = tradeUtil.contractsToAmount(contracts);
        final OrderCommand orderCommand = tradeUtil.orderCommandForContracts(contracts);
        final double slPrice = tradeUtil.calculateSL(instrument,
                                                     orderCommand,
                                                     dStopDist);
        final OrderActionResult submitResult = orderSubmit.run(instrument,
                                                               orderCommand,
                                                               amount,
                                                               label,
                                                               slPrice);

        return submitResult;
    }
}
