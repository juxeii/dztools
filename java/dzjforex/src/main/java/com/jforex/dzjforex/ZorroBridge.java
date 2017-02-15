package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.connection.CredentialsFactory;
import com.jforex.dzjforex.connection.LoginHandler;
import com.jforex.dzjforex.connection.PinProvider;
import com.jforex.dzjforex.datetime.DateTimeUtils;
import com.jforex.dzjforex.datetime.ServerTime;
import com.jforex.dzjforex.handler.AccountHandler;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.dzjforex.handler.SubscriptionHandler;
import com.jforex.dzjforex.history.HistoryHandler;
import com.jforex.dzjforex.misc.StrategyForData;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.strategy.StrategyUtil;

public class ZorroBridge {

    private IClient client;
    private final ClientUtil clientUtil;
    private IContext context;
    private final Authentification authentification;
    private final PinProvider pinProvider;
    private final LoginHandler loginHandler;
    private final StrategyForData strategyForData;
    private long strategyID;
    private final CredentialsFactory credentialsFactory;
    private AccountHandler accountHandler;
    private ServerTime serverTime;
    private DateTimeUtils dateTimeUtils;
    private SubscriptionHandler subscriptionHandler;
    private OrderHandler orderHandler;
    private HistoryHandler historyHandler;

    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        initClientInstance();

        clientUtil = new ClientUtil(client, pluginConfig.CACHE_DIR());
        authentification = clientUtil.authentification();
        pinProvider = new PinProvider(client, pluginConfig.CONNECT_URL_REAL());
        credentialsFactory = new CredentialsFactory(pinProvider, pluginConfig);
        loginHandler = new LoginHandler(client,
                                        authentification,
                                        credentialsFactory);
        strategyForData = new StrategyForData();
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
        ZorroLogger.indicateError();
    }

    public int doLogin(final String userName,
                       final String password,
                       final String type,
                       final String accountHandlers[]) {
        ZorroLogger.log("doLogin called");
        if (client.isConnected())
            return ReturnCodes.LOGIN_OK;

        final int loginResult = loginHandler.doLogin(userName,
                                                     password,
                                                     type);
        if (loginResult == ReturnCodes.LOGIN_OK) {
            startStrategy();
            fillaccountHandlers(accountHandlers);
        }

        return loginResult;
    }

    private void startStrategy() {
        ZorroLogger.log("starting strategy...");
        strategyID = client.startStrategy(strategyForData);
        context = strategyForData.getContext();
        final StrategyUtil strategyUtil = strategyForData.strategyUtil();

        serverTime = new ServerTime(strategyForData, pluginConfig);
        dateTimeUtils = new DateTimeUtils(context.getDataService(), serverTime);
        accountHandler = new AccountHandler(context.getAccount(),
                                            strategyUtil,
                                            strategyUtil.calculationUtil(),
                                            pluginConfig);
        subscriptionHandler = new SubscriptionHandler(client, accountHandler);
        orderHandler = new OrderHandler(context,
                                        strategyUtil,
                                        pluginConfig);
        historyHandler = new HistoryHandler(context.getHistory());
    }

    private void fillaccountHandlers(final String accountHandlers[]) {
        final String accountID = accountHandler.getID();
        accountHandlers[0] = accountID;
        ZorroLogger.log("Filled account infos with " + accountID);
    }

    public int doLogout() {
        ZorroLogger.log("doLogout called");
        client.stopStrategy(strategyID);
        return loginHandler.doLogout();
    }

    public int doBrokerTime(final double serverTimeData[]) {
        ZorroLogger.log("doBrokerTime called");
        if (!client.isConnected())
            return ReturnCodes.CONNECTION_LOST_NEW_LOGIN_REQUIRED;

        return dateTimeUtils.doBrokerTime(serverTimeData);
    }

    public int doSubscribeAsset(final String instrumentName) {
        ZorroLogger.log("doSubscribeAsset called");
        if (!client.isConnected())
            return ReturnCodes.ASSET_UNAVAILABLE;

        return subscriptionHandler.doSubscribeAsset(instrumentName);
    }

    public int doBrokerAsset(final String instrumentName,
                             final double assetParams[]) {
        ZorroLogger.log("doBrokerAsset called");
        if (!client.isConnected())
            return ReturnCodes.ASSET_UNAVAILABLE;

        return accountHandler.doBrokerAsset(instrumentName, assetParams);
    }

    public int doBrokerAccount(final double accountHandlerParams[]) {
        ZorroLogger.log("doBrokerAccount called");
        if (!accountHandler.isConnected())
            return ReturnCodes.ACCOUNT_UNAVAILABLE;

        return accountHandler.doBrokerAccount(accountHandlerParams);
    }

    public int doBrokerBuy(final String instrumentName,
                           final double tradeParams[]) {
        ZorroLogger.log("doBrokerBuy called");
        if (!accountHandler.isTradingPossible())
            return ReturnCodes.BROKER_BUY_FAIL;

        return orderHandler.doBrokerBuy(instrumentName, tradeParams);
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        ZorroLogger.log("doBrokerTrade called");
        if (!accountHandler.isTradingPossible())
            return ReturnCodes.UNKNOWN_ORDER_ID;

        return orderHandler.doBrokerTrade(orderID, orderParams);
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        ZorroLogger.log("doBrokerStop called");
        if (!accountHandler.isTradingPossible())
            return ReturnCodes.UNKNOWN_ORDER_ID;

        return orderHandler.doBrokerStop(orderID, newSLPrice);
    }

    public int doBrokerSell(final int orderID,
                            final int amount) {
        ZorroLogger.log("doBrokerSell called");
        if (!accountHandler.isTradingPossible())
            return ReturnCodes.BROKER_SELL_FAIL;

        return orderHandler.doBrokerSell(orderID, amount);
    }

    public int doBrokerHistory2(final String instrumentName,
                                final double startDate,
                                final double endDate,
                                final int tickMinutes,
                                final int nTicks,
                                final double tickParams[]) {
        ZorroLogger.log("doBrokerHistory2 called");
        if (!accountHandler.isConnected())
            return ReturnCodes.HISTORY_UNAVAILABLE;

        return historyHandler.doBrokerHistory2(instrumentName,
                                               startDate,
                                               endDate,
                                               tickMinutes,
                                               nTicks,
                                               tickParams);
    }

    public int doHistoryDownload() {
        ZorroLogger.log("doHistoryDownload called");
        if (!client.isConnected())
            return ReturnCodes.HISTORY_DOWNLOAD_FAIL;

        return historyHandler.doHistoryDownload();
    }
}
