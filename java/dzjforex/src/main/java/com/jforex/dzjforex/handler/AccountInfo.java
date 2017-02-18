package com.jforex.dzjforex.handler;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IAccount.AccountState;
import com.dukascopy.api.ICurrency;
import com.jforex.dzjforex.config.PluginConfig;

public class AccountInfo {

    private final IAccount account;
    private final PluginConfig pluginConfig;

    public AccountInfo(final IAccount account,
                       final PluginConfig pluginConfig) {
        this.account = account;
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
}
