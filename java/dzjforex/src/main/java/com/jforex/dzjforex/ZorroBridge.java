package com.jforex.dzjforex;

import java.time.Clock;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        final int loginResult = loginHandler.brokerLogin(User,
                                                         Pwd,
                                                         Type);
        if (loginResult == ZorroReturnValues.LOGIN_OK.getValue()) {
            initComponents();
            accountHandler.fillAcountInfos(Accounts);
        }

        return loginResult;
    }

    public int doLogout() {
//        client.stopStrategy(strategyID);
//        return brokerLogin.logout();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        return timeHandler.brokerTime(pTimeUTC);
    }

    public int doSubscribeAsset(final String Asset) {
        return accountHandler.subscribeAsset(Asset);
    }

    public int doBrokerAsset(final String Asset,
                             final double assetParams[]) {
        return accountHandler.brokerAsset(Asset, assetParams);
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        return accountHandler.brokerAccount(accountInfoParams);
    }

    public int doBrokerTrade(final int nTradeID,
                             final double orderParams[]) {
        return tradeHandler.brokerTrade(nTradeID, orderParams);
    }

    public int doBrokerBuy(final String Asset,
                           final double tradeParams[]) {
        return tradeHandler.brokerBuy(Asset, tradeParams);
    }

    public int doBrokerSell(final int nTradeID,
                            final int nAmount) {
        return tradeHandler.brokerSell(nTradeID, nAmount);
    }

    public int doBrokerStop(final int nTradeID,
                            final double dStop) {
        return tradeHandler.brokerStop(nTradeID, dStop);
    }

    public int doBrokerHistory2(final String Asset,
                                final double tStart,
                                final double tEnd,
                                final int nTickMinutes,
                                final int nTicks,
                                final double tickParams[]) {
        return historyHandler.brokerHistory2(Asset,
                                             tStart,
                                             tEnd,
                                             nTickMinutes,
                                             nTicks,
                                             tickParams);
    }

    public int doSetOrderText(final String orderText) {
        Zorro.logError("doSetOrderText for " + orderText + " called but not yet supported!");
        return ZorroReturnValues.BROKER_COMMAND_OK.getValue();
    }
}
