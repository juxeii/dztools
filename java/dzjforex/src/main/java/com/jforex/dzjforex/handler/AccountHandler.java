package com.jforex.dzjforex.handler;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerAccount;
import com.jforex.dzjforex.brokerapi.BrokerAsset;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.AccountInfo;
import com.jforex.dzjforex.misc.InfoStrategy;

public class AccountHandler {

    private final AccountInfo accountInfo;
    private final BrokerAsset brokerAsset;
    private final BrokerAccount brokerAccount;
    private final BrokerSubscribe brokerSubscribe;

    public AccountHandler(final IClient client,
                          final InfoStrategy infoStrategy,
                          final PluginConfig pluginConfig) {
        accountInfo = new AccountInfo(infoStrategy.getContext().getAccount(),
                                      infoStrategy.strategyUtil().calculationUtil(),
                                      pluginConfig);
        brokerSubscribe = new BrokerSubscribe(client, accountInfo);
        brokerAsset = new BrokerAsset(accountInfo, infoStrategy.strategyUtil());
        brokerAccount = new BrokerAccount(accountInfo);
    }

    public BrokerSubscribe brokerSubscribe() {
        return brokerSubscribe;
    }

    public AccountInfo accountInfo() {
        return accountInfo;
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
