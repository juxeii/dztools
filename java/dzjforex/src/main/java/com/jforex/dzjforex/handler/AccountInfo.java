package com.jforex.dzjforex.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IAccount.AccountState;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.math.CalculationUtil;

public class AccountInfo {

    private final IAccount account;
    private final CalculationUtil calculationUtil;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(AccountInfo.class);

    public AccountInfo(final IAccount account,
                       final CalculationUtil calculationUtil,
                       final PluginConfig pluginConfig) {
        this.account = account;
        this.calculationUtil = calculationUtil;
        this.pluginConfig = pluginConfig;
    }

    public AccountState state() {
        return account.getAccountState();
    }

    public String id() {
        return account.getAccountId();
    }

    public double balance() {
        return account.getBalance();
    }

    public double equity() {
        return account.getEquity();
    }

    public double baseEquity() {
        return account.getBaseEquity();
    }

    public ICurrency currency() {
        return account.getAccountCurrency();
    }

    public double lotSize() {
        return pluginConfig.LOT_SIZE();
    }

    public double lotMargin() {
        return lotSize() / leverage();
    }

    public double tradeValue() {
        return account.getEquity() - account.getBaseEquity();
    }

    public double freeMargin() {
        return account.getCreditLine() / leverage();
    }

    public double usedMargin() {
        return account.getEquity() - freeMargin();
    }

    public double leverage() {
        return account.getLeverage();
    }

    public boolean isConnected() {
        return account.isConnected();
    }

    public boolean isNFACompliant() {
        return account.isGlobal();
    }

    public boolean isTradingAllowed() {
        return account.getAccountState() == IAccount.AccountState.OK
                || account.getAccountState() == IAccount.AccountState.OK_NO_MARGIN_CALL;
    }

    public double pipCost(final Instrument instrument) {
        final double pipCost = calculationUtil.pipValueInCurrency(lotSize(),
                                                                  instrument,
                                                                  currency(),
                                                                  OfferSide.ASK);
        logger.debug("Pipcost for lotSize " + lotSize()
                + " and instrument " + instrument
                + " is " + pipCost);
        return pipCost;
    }

    public double marginPerLot(final Instrument instrument) {
        final ICurrency accountCurrency = currency();
        if (accountCurrency == instrument.getPrimaryJFCurrency())
            return lotMargin();

        final double conversionLot = calculationUtil.convertAmount(lotSize(),
                                                                   instrument.getPrimaryJFCurrency(),
                                                                   accountCurrency,
                                                                   OfferSide.ASK);
        final double marginCost = conversionLot / leverage();
        logger.debug("marginCost for conversion instrument " + instrument.getPrimaryJFCurrency()
                + " and  conversionLot " + conversionLot
                + " and leverage " + leverage());
        return marginCost;
    }
}
