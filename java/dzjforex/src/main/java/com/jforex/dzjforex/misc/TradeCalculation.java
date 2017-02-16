package com.jforex.dzjforex.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.programming.math.CalculationUtil;

public class TradeCalculation {

    private final CalculationUtil calculationUtil;
    private final ICurrency accountCurrency;
    private final double leverage;
    private final double lotSize;
    private final double lotMargin;

    private final static Logger logger = LogManager.getLogger(TradeCalculation.class);

    public TradeCalculation(final AccountInfo accountInfo,
                            final CalculationUtil calculationUtil) {
        this.calculationUtil = calculationUtil;

        accountCurrency = accountInfo.currency();
        leverage = accountInfo.leverage();
        lotSize = accountInfo.lotSize();
        lotMargin = lotSize / leverage;
    }

    public double pipCost(final Instrument instrument) {
        final double pipCost = calculationUtil.pipValueInCurrency(lotSize,
                                                                  instrument,
                                                                  accountCurrency,
                                                                  OfferSide.ASK);
        logger.debug("Pipcost for lotSize " + lotSize
                + " and instrument " + instrument
                + " is " + pipCost);
        return pipCost;
    }

    public double marginPerLot(final Instrument instrument) {
        if (accountCurrency == instrument.getPrimaryJFCurrency())
            return lotMargin;

        final double conversionLot = calculationUtil.convertAmount(lotSize,
                                                                   instrument.getPrimaryJFCurrency(),
                                                                   accountCurrency,
                                                                   OfferSide.ASK);
        final double marginCost = conversionLot / leverage;
        logger.debug("marginCost for conversion instrument " + instrument.getPrimaryJFCurrency()
                + " and  conversionLot " + conversionLot
                + " and leverage " + leverage);
        return marginCost;
    }
}
