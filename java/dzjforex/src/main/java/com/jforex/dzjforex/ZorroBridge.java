package com.jforex.dzjforex;

import java.time.Clock;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.AccountHandler;
import com.jforex.dzjforex.handler.HistoryHandler;
import com.jforex.dzjforex.handler.LoginHandler;
import com.jforex.dzjforex.handler.TimeHandler;
import com.jforex.dzjforex.handler.TradeHandler;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.programming.client.ClientUtil;

public class ZorroBridge {

    private IClient client;
    private final ClientUtil clientUtil;
    private final InfoStrategy infoStrategy;
    private long strategyID;
    private final Zorro zorro;
    private AccountHandler accountHandler;
    private final LoginHandler loginHandler;
    private HistoryHandler historyHandler;
    private TradeHandler tradeHandler;

    private TimeHandler timeHandler;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        initClientInstance();

        zorro = new Zorro(pluginConfig);
        clientUtil = new ClientUtil(client, pluginConfig.cacheDirectory());
        loginHandler = new LoginHandler(clientUtil,
                                        zorro,
                                        pluginConfig);
        infoStrategy = new InfoStrategy();
    }

    private void initClientInstance() {
        try {
            client = ClientFactory.getDefaultInstance();
            logger.debug("IClient successfully initialized.");
            return;
        } catch (final ClassNotFoundException e) {
            logger.error("IClient ClassNotFoundException occured! " + e.getMessage());
        } catch (final IllegalAccessException e) {
            logger.error("IClient IllegalAccessException occured!" + e.getMessage());
        } catch (final InstantiationException e) {
            logger.error("IClient InstantiationException occured!" + e.getMessage());
        }
        Zorro.indicateError();
    }

    private void initComponents() {
        accountHandler = new AccountHandler(client,
                                            infoStrategy,
                                            pluginConfig);

        timeHandler = new TimeHandler(Clock.systemDefaultZone(),
                                      client,
                                      infoStrategy,
                                      pluginConfig);
        historyHandler = new HistoryHandler(infoStrategy.getContext().getHistory(), pluginConfig);
        tradeHandler = new TradeHandler(historyHandler.historyProvider(),
                                        accountHandler,
                                        timeHandler,
                                        infoStrategy,
                                        pluginConfig);
    }

    public int doLogin(final String userName,
                       final String password,
                       final String type,
                       final String accountInfos[]) {
        final int loginResult = loginHandler.brokerLogin(userName,
                                                         password,
                                                         type);
        if (loginResult == ZorroReturnValues.LOGIN_OK.getValue()) {
            strategyID = client.startStrategy(infoStrategy);
            initComponents();
            accountInfos[0] = accountHandler.accountInfo().id();
        }

        return loginResult;
    }

    public int doLogout() {
        logger.info("Logout called");
//        client.stopStrategy(strategyID);
//        return brokerLogin.logout();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        return timeHandler.brokerTime(pTimeUTC);
    }

    public int doSubscribeAsset(final String instrumentName) {
        return accountHandler.subscribeAsset(instrumentName);
    }

    public int doBrokerAsset(final String instrumentName,
                             final double assetParams[]) {
        return accountHandler.brokerAsset(instrumentName, assetParams);
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        return accountHandler.brokerAccount(accountInfoParams);
    }

    public int doBrokerBuy(final String instrumentName,
                           final double tradeParams[]) {
        return tradeHandler.brokerBuy(instrumentName, tradeParams);
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        return tradeHandler.brokerTrade(orderID, orderParams);
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        return tradeHandler.brokerStop(orderID, newSLPrice);
    }

    public int doBrokerSell(final int nTradeID,
                            final int nAmount) {
        return tradeHandler.brokerSell(nTradeID, nAmount);
    }

    public int doBrokerHistory2(final String instrumentName,
                                final double startDate,
                                final double endDate,
                                final int tickMinutes,
                                final int nTicks,
                                final double tickParams[]) {
        return historyHandler.brokerHistory2(instrumentName,
                                             startDate,
                                             endDate,
                                             tickMinutes,
                                             nTicks,
                                             tickParams);
    }

    public int doSetOrderText(final String orderText) {
        Zorro.logError("doSetOrderText for " + orderText + " called but not yet supported!");
        return ZorroReturnValues.BROKER_COMMAND_OK.getValue();
    }
}
