package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.handler.AccountInfo;

public class BrokerAccount {

    private final AccountInfo accountInfo;

    private final static Logger logger = LogManager.getLogger(BrokerBuy.class);

    public BrokerAccount(final AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public int handle(final double accountInfoParams[]) {
        if (!isAccountAvailable())
            return Constant.ACCOUNT_UNAVAILABLE;

        fillAccountParams(accountInfoParams);
        return Constant.ACCOUNT_AVAILABLE;
    }

    private boolean isAccountAvailable() {
        return accountInfo.isConnected();
    }

    private void fillAccountParams(final double accountInfoParams[]) {
        accountInfoParams[0] = accountInfo.baseEquity();
        accountInfoParams[1] = accountInfo.tradeValue();
        accountInfoParams[2] = accountInfo.usedMargin();

        logger.debug("BrokerAccount call: \n"
                + "equity:  " + accountInfoParams[0] + "\n"
                + "tradeValue:  " + accountInfoParams[1] + "\n"
                + "usedMargin:  " + accountInfoParams[2] + "\n");
    }
}
