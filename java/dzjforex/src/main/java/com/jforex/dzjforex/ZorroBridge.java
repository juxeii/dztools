package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;

import com.jforex.dzjforex.brokerapi.BrokerAccount;
import com.jforex.dzjforex.brokerapi.BrokerAccountData;
import com.jforex.dzjforex.brokerapi.BrokerAsset;
import com.jforex.dzjforex.brokerapi.BrokerAssetData;
import com.jforex.dzjforex.brokerapi.BrokerBuy;
import com.jforex.dzjforex.brokerapi.BrokerBuyData;
import com.jforex.dzjforex.brokerapi.BrokerHistory;
import com.jforex.dzjforex.brokerapi.BrokerHistoryData;
import com.jforex.dzjforex.brokerapi.BrokerLoginData;
import com.jforex.dzjforex.brokerapi.BrokerSell;
import com.jforex.dzjforex.brokerapi.BrokerSellData;
import com.jforex.dzjforex.brokerapi.BrokerStop;
import com.jforex.dzjforex.brokerapi.BrokerStopData;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.brokerapi.BrokerTime;
import com.jforex.dzjforex.brokerapi.BrokerTimeData;
import com.jforex.dzjforex.brokerapi.BrokerTrade;
import com.jforex.dzjforex.brokerapi.BrokerTradeData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.login.LoginHandler;
import com.jforex.dzjforex.misc.Components;

public class ZorroBridge {

    private final Components components;
    private final LoginHandler loginHandler;
    private BrokerTime brokerTime;
    private BrokerSubscribe brokerSubscribe;
    private BrokerAsset brokerAsset;
    private BrokerAccount brokerAccount;
    private BrokerTrade brokerTrade;
    private BrokerBuy brokerBuy;
    private BrokerSell brokerSell;
    private BrokerStop brokerStop;
    private BrokerHistory brokerHistory;
    private long strategyID;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    public ZorroBridge() {
        components = new Components(pluginConfig);
        loginHandler = components.loginHandler();
    }

    private void initComponents(final BrokerLoginData brokerLoginData) {
        strategyID = components.startAndInitStrategyComponents(brokerLoginData);

        brokerTime = components.brokerTime();
        brokerSubscribe = components.brokerSubscribe();
        brokerAsset = components.brokerAsset();
        brokerAccount = components.brokerAccount();
        brokerTrade = components.brokerTrade();
        brokerBuy = components.brokerBuy();
        brokerSell = components.brokerSell();
        brokerStop = components.brokerStop();
        brokerHistory = components.brokerHistory();
    }

    public int doLogin(final String User,
                       final String Pwd,
                       final String Type,
                       final String Accounts[]) {
        final BrokerLoginData brokerLoginData = new BrokerLoginData(User,
                                                                    Pwd,
                                                                    Type,
                                                                    Accounts);
        final int loginResult = loginHandler.login(brokerLoginData);
        if (loginResult == ZorroReturnValues.LOGIN_OK.getValue())
            initComponents(brokerLoginData);

        return loginResult;
    }

    public int doLogout() {
//        client.stopStrategy(strategyID);
//        return brokerLogin.logout();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        final BrokerTimeData brokerTimeData = new BrokerTimeData(pTimeUTC);
        return brokerTime.get(brokerTimeData);
    }

    public int doSubscribeAsset(final String Asset) {
        return brokerSubscribe.subscribe(Asset);
    }

    public int doBrokerAsset(final String Asset,
                             final double assetParams[]) {
        final BrokerAssetData brokerAssetData = new BrokerAssetData(Asset, assetParams);
        return brokerAsset.fillAssetParams(brokerAssetData);
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        final BrokerAccountData brokerAccountData = new BrokerAccountData(accountInfoParams);
        return brokerAccount.handle(brokerAccountData);
    }

    public int doBrokerTrade(final int nTradeID,
                             final double tradeParams[]) {
        final BrokerTradeData brokerTradeData = new BrokerTradeData(nTradeID, tradeParams);
        return brokerTrade.orderInfo(brokerTradeData);
    }

    public int doBrokerBuy(final String Asset,
                           final double tradeParams[]) {
        final BrokerBuyData brokerBuyData = new BrokerBuyData(Asset, tradeParams);
        return brokerBuy.openTrade(brokerBuyData);
    }

    public int doBrokerSell(final int nTradeID,
                            final int nAmount) {
        final BrokerSellData brokerSellData = new BrokerSellData(nTradeID, nAmount);
        return brokerSell.closeTrade(brokerSellData);
    }

    public int doBrokerStop(final int nTradeID,
                            final double dStop) {
        final BrokerStopData brokerStopData = new BrokerStopData(nTradeID, dStop);
        return brokerStop.setSL(brokerStopData);
    }

    public int doBrokerHistory2(final String Asset,
                                final double tStart,
                                final double tEnd,
                                final int nTickMinutes,
                                final int nTicks,
                                final double tickParams[]) {
        final BrokerHistoryData brokerHistoryData = new BrokerHistoryData(Asset,
                                                                          tStart,
                                                                          tEnd,
                                                                          nTickMinutes,
                                                                          nTicks,
                                                                          tickParams);
        return brokerHistory.get(brokerHistoryData);
    }

    public int doSetOrderText(final String orderText) {
        Zorro.logDiagnose("doSetOrderText for " + orderText + " called but not yet supported!");
        return ZorroReturnValues.BROKER_COMMAND_OK.getValue();
    }
}
