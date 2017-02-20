package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.order.SubmitHandler;
import com.jforex.dzjforex.order.TradeUtil;

public class BrokerBuy {

    private final SubmitHandler submitHandler;
    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerBuy.class);

    public BrokerBuy(final SubmitHandler submitHandler,
                     final TradeUtil tradeUtil) {
        this.submitHandler = submitHandler;
        this.tradeUtil = tradeUtil;
    }

    public int openTrade(final String assetName,
                         final double tradeParams[]) {
        final Optional<Instrument> maybeInstrument = tradeUtil.maybeInstrumentForTrading(assetName);
        if (!maybeInstrument.isPresent())
            return Constant.BROKER_BUY_FAIL;

        logger.info("Trying to open trade for assetName " + assetName + "\n"
                + "nAmount:  " + tradeParams[0] + "\n"
                + "dStopDist:  " + tradeParams[1]);
        return submit(maybeInstrument.get(), tradeParams);
    }

    private int submit(final Instrument instrument,
                       final double tradeParams[]) {
        final IOrder order = getSubmitOrder(instrument, tradeParams);
        if (order == null)
            return Constant.BROKER_BUY_FAIL;

        final int orderID = Integer.parseInt(order.getId());
        tradeUtil.storeOrder(orderID, order);
        tradeParams[2] = order.getOpenPrice();
        final double dStopDist = tradeParams[1];

        return dStopDist == -1
                ? Constant.BROKER_BUY_OPPOSITE_CLOSE
                : orderID;
    }

    private IOrder getSubmitOrder(final Instrument instrument,
                                  final double tradeParams[]) {
        final double contracts = tradeParams[0];
        final double dStopDist = tradeParams[1];

        final double amount = tradeUtil.contractsToAmount(contracts);
        final OrderCommand orderCommand = tradeUtil.orderCommandForContracts(contracts);
        final double slPrice = tradeUtil.calculateSL(instrument,
                                                     orderCommand,
                                                     dStopDist);
        final String label = tradeUtil.createLabel();

        return submitHandler.submit(instrument,
                                    orderCommand,
                                    amount,
                                    label,
                                    slPrice);
    }
}
