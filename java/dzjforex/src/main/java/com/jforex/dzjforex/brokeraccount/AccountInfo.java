package com.jforex.dzjforex.brokeraccount;

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
        return pluginConfig.lotSize();
    }

    public double lotMargin() {
        return lotSize() / leverage();
    }

    public double tradeValue() {
        return equity() - baseEquity();
    }

    public double freeMargin() {
        return account.getCreditLine() / leverage();
    }

    public double usedMargin() {
        return equity() - freeMargin();
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
        return state() == IAccount.AccountState.OK ||
                state() == IAccount.AccountState.OK_NO_MARGIN_CALL;
    }

    public double pipCost(final Instrument instrument) {
        return calculationUtil.pipValueInCurrency(lotSize(),
                                                  instrument,
                                                  currency(),
                                                  OfferSide.ASK);
    }

    public double marginPerLot(final Instrument instrument) {
        final ICurrency primaryCurrency = instrument.getPrimaryJFCurrency();

        return currency() == primaryCurrency
                ? lotMargin()
                : marginForCrossCurrency(primaryCurrency);
    }

    private double marginForCrossCurrency(final ICurrency crossCurrency) {
        final double conversionLot = calculationUtil.convertAmount(lotSize(),
                                                                   crossCurrency,
                                                                   currency(),
                                                                   OfferSide.ASK);
        return conversionLot / leverage();
    }
}
