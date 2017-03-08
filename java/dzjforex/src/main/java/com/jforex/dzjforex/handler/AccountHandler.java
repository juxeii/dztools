package com.jforex.dzjforex.handler;

import com.jforex.dzjforex.brokerapi.BrokerAccount;
import com.jforex.dzjforex.brokerapi.BrokerAsset;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.misc.AccountInfo;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.programming.strategy.StrategyUtil;

public class AccountHandler {

    private final AccountInfo accountInfo;
    private final BrokerAsset brokerAsset;
    private final BrokerAccount brokerAccount;
    private final BrokerSubscribe brokerSubscribe;

    public AccountHandler(final SystemHandler systemHandler) {
        final InfoStrategy infoStrategy = systemHandler.infoStrategy();
        final StrategyUtil strategyUtil = infoStrategy.strategyUtil();

        accountInfo = new AccountInfo(infoStrategy.getAccount(),
                                      strategyUtil.calculationUtil(),
                                      systemHandler.pluginConfig());
        brokerAccount = new BrokerAccount(accountInfo);
        brokerAsset = new BrokerAsset(accountInfo, strategyUtil);
        brokerSubscribe = new BrokerSubscribe(systemHandler.client(), accountInfo);
    }

    public BrokerSubscribe brokerSubscribe() {
        return brokerSubscribe;
    }

    public AccountInfo accountInfo() {
        return accountInfo;
    }

    public void fillAcountInfos(final String accountInfos[]) {
        accountInfos[0] = accountInfo().id();
    }

    public int subscribeAsset(final String instrumentName) {
        return brokerSubscribe.subscribe(instrumentName);
    }

    public int brokerAsset(final String instrumentName,
                           final double assetParams[]) {
        return brokerAsset.fillAssetParams(instrumentName, assetParams);
    }

    public int brokerAccount(final double accountInfoParams[]) {
        return brokerAccount.handle(accountInfoParams);
    }
}
