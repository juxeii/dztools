package com.jforex.dzjforex;

import java.time.Clock;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.brokerapi.BrokerAccountData;
import com.jforex.dzjforex.brokerapi.BrokerAssetData;
import com.jforex.dzjforex.brokerapi.BrokerBuyData;
import com.jforex.dzjforex.brokerapi.BrokerHistoryData;
import com.jforex.dzjforex.brokerapi.BrokerLoginData;
import com.jforex.dzjforex.brokerapi.BrokerSellData;
import com.jforex.dzjforex.brokerapi.BrokerStopData;
import com.jforex.dzjforex.brokerapi.BrokerTimeData;
import com.jforex.dzjforex.brokerapi.BrokerTradeData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.AccountHandler;
import com.jforex.dzjforex.handler.HistoryHandler;
import com.jforex.dzjforex.handler.LoginHandler;
import com.jforex.dzjforex.handler.SystemHandler;
import com.jforex.dzjforex.handler.TimeHandler;
import com.jforex.dzjforex.handler.TradeHandler;

public class ZorroBridge {

    private final SystemHandler systemHandler;
    private AccountHandler accountHandler;
    private final LoginHandler loginHandler;
    private HistoryHandler historyHandler;
    private TradeHandler tradeHandler;
    private TimeHandler timeHandler;
    private long strategyID;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        systemHandler = new SystemHandler(pluginConfig, Clock.systemDefaultZone());
        loginHandler = new LoginHandler(systemHandler);
    }

    private void initComponents() {
        strategyID = systemHandler.startStrategy();

        accountHandler = new AccountHandler(systemHandler);
        timeHandler = new TimeHandler(systemHandler);
        historyHandler = new HistoryHandler(systemHandler);
        tradeHandler = new TradeHandler(systemHandler,
                                        accountHandler,
                                        timeHandler,
                                        historyHandler);
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
        if (loginResult == ZorroReturnValues.LOGIN_OK.getValue()) {
            initComponents();
            accountHandler.fillAcountInfos(brokerLoginData);
        }

        return loginResult;
    }

    public int doLogout() {
//        client.stopStrategy(strategyID);
//        return brokerLogin.logout();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        final BrokerTimeData brokerTimeData = new BrokerTimeData(pTimeUTC);
        return timeHandler.brokerTime(brokerTimeData);
    }

    public int doSubscribeAsset(final String Asset) {
        return accountHandler.subscribeAsset(Asset);
    }

    public int doBrokerAsset(final String Asset,
                             final double assetParams[]) {
        final BrokerAssetData brokerAssetData = new BrokerAssetData(Asset, assetParams);
        return accountHandler.brokerAsset(brokerAssetData);
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        final BrokerAccountData brokerAccountData = new BrokerAccountData(accountInfoParams);
        return accountHandler.brokerAccount(brokerAccountData);
    }

    public int doBrokerTrade(final int nTradeID,
                             final double tradeParams[]) {
        final BrokerTradeData brokerTradeData = new BrokerTradeData(nTradeID, tradeParams);
        return tradeHandler.brokerTrade(brokerTradeData);
    }

    public int doBrokerBuy(final String Asset,
                           final double tradeParams[]) {
        final BrokerBuyData brokerBuyData = new BrokerBuyData(Asset, tradeParams);
        return tradeHandler.brokerBuy(brokerBuyData);
    }

    public int doBrokerSell(final int nTradeID,
                            final int nAmount) {
        final BrokerSellData brokerSellData = new BrokerSellData(nTradeID, nAmount);
        return tradeHandler.brokerSell(brokerSellData);
    }

    public int doBrokerStop(final int nTradeID,
                            final double dStop) {
        final BrokerStopData brokerStopData = new BrokerStopData(nTradeID, dStop);
        return tradeHandler.brokerStop(brokerStopData);
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
        return historyHandler.brokerHistory(brokerHistoryData);
    }

    public int doSetOrderText(final String orderText) {
        Zorro.logDiagnose("doSetOrderText for " + orderText + " called but not yet supported!");
        return ZorroReturnValues.BROKER_COMMAND_OK.getValue();
    }
}
