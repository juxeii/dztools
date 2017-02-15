package com.jforex.dzjforex.dataprovider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.ICurrency;

public class AccountInfo {

    private final IAccount account;
    private final ICurrency accountCurrency;
    private final String accountID;
    private final double leverage;

    private final static Logger logger = LogManager.getLogger(AccountInfo.class);

    public AccountInfo(final IAccount account) {
        this.account = account;

        accountCurrency = account.getAccountCurrency();
        accountID = account.getAccountId();
        leverage = account.getLeverage();
    }

    public double getBalance() {
        return account.getBalance();
    }

    public double getEquity() {
        return account.getEquity();
    }

    public String getID() {
        return accountID;
    }

    public ICurrency getCurrency() {
        return accountCurrency;
    }

    public double getTradeValue() {
        return account.getEquity() - account.getBalance();
    }

    public double getFreeMargin() {
        return account.getCreditLine() / leverage;
    }

    public double getUsedMargin() {
        return account.getEquity() - getFreeMargin();
    }

    public double getLeverage() {
        return leverage;
    }

    public boolean isConnected() {
        return account.isConnected();
    }

    public boolean isTradingPossible() {
        if (account.getAccountState() != IAccount.AccountState.OK) {
            logger.debug("Account state " + account.getAccountState() + " is invalid for trading!");
            return false;
        }
        return account.isConnected();
    }
}
