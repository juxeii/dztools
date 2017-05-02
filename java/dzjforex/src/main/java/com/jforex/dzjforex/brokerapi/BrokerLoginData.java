package com.jforex.dzjforex.brokerapi;

import com.jforex.dzjforex.misc.AccountInfo;

public class BrokerLoginData {

    private final String username;
    private final String password;
    private final String loginType;
    private final String accounts[];

    public BrokerLoginData(final String username,
                           final String password,
                           final String loginType,
                           final String accounts[]) {
        this.username = username;
        this.password = password;
        this.loginType = loginType;
        this.accounts = accounts;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String loginType() {
        return loginType;
    }

    public void fillAccounts(final AccountInfo accountInfo) {
        accounts[0] = accountInfo.id();
    }
}
