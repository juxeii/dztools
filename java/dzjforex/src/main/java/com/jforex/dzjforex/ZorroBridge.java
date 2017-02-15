package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.dataprovider.AccountInfo;
import com.jforex.dzjforex.dataprovider.ServerTime;
import com.jforex.dzjforex.handler.AccountHandler;
import com.jforex.dzjforex.handler.HistoryHandler;
import com.jforex.dzjforex.handler.LoginHandler;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.dzjforex.handler.SubscriptionHandler;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.misc.DateTimeUtils;
import com.jforex.dzjforex.misc.PinProvider;
import com.jforex.dzjforex.misc.StrategyForData;
import com.jforex.dzjforex.settings.PluginConfig;
import com.jforex.dzjforex.settings.ReturnCodes;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.connection.Authentification;

public class ZorroBridge {

    private IClient client;
    private final ClientUtil clientUtil;
    private IContext context;
    private final Authentification authentification;
    private final PinProvider pinProvider;
    private final LoginHandler loginHandler;
    private AccountInfo accountInfo;
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
                       final String accountInfos[]) {
        ZorroLogger.log("doLogin called");
        if (client.isConnected())
            return ReturnCodes.LOGIN_OK;

        final int loginResult = loginHandler.doLogin(userName,
                                                     password,
                                                     type);
        if (loginResult == ReturnCodes.LOGIN_OK) {
            startStrategy();
            fillAccountInfos(accountInfos);
        }

        return loginResult;
    }

    private void startStrategy() {
        ZorroLogger.log("starting strategy...");
        strategyID = client.startStrategy(strategyForData);
        context = strategyForData.getContext();

        accountInfo = new AccountInfo(context.getAccount());
        serverTime = new ServerTime(strategyForData);
        dateTimeUtils = new DateTimeUtils(context.getDataService(), serverTime);
        accountHandler = new AccountHandler(strategyForData.strategyUtil(),
                                            accountInfo,
                                            serverTime,
                                            dateTimeUtils);
        subscriptionHandler = new SubscriptionHandler(client, accountInfo);
        orderHandler = new OrderHandler(context, strategyForData.strategyUtil());
        historyHandler = new HistoryHandler(context.getHistory());
    }

    private void fillAccountInfos(final String accountInfos[]) {
        final String accountID = accountInfo.getID();
        accountInfos[0] = accountID;
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

        return accountHandler.doBrokerTime(serverTimeData);
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

    public int doBrokerAccount(final double accountInfoParams[]) {
        ZorroLogger.log("doBrokerAccount called");
        if (!accountInfo.isConnected())
            return ReturnCodes.ACCOUNT_UNAVAILABLE;

        return accountHandler.doBrokerAccount(accountInfoParams);
    }

    public int doBrokerBuy(final String instrumentName,
                           final double tradeParams[]) {
        ZorroLogger.log("doBrokerBuy called");
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.BROKER_BUY_FAIL;

        return orderHandler.doBrokerBuy(instrumentName, tradeParams);
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        ZorroLogger.log("doBrokerTrade called");
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.UNKNOWN_ORDER_ID;

        return orderHandler.doBrokerTrade(orderID, orderParams);
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        ZorroLogger.log("doBrokerStop called");
        if (!accountInfo.isTradingPossible())
            return ReturnCodes.UNKNOWN_ORDER_ID;

        return orderHandler.doBrokerStop(orderID, newSLPrice);
    }

    public int doBrokerSell(final int orderID,
                            final int amount) {
        ZorroLogger.log("doBrokerSell called");
        if (!accountInfo.isTradingPossible())
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
        if (!accountInfo.isConnected())
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
