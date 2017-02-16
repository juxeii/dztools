package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerAccount;
import com.jforex.dzjforex.brokerapi.BrokerAsset;
import com.jforex.dzjforex.brokerapi.BrokerLogin;
import com.jforex.dzjforex.brokerapi.BrokerStop;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.brokerapi.BrokerTime;
import com.jforex.dzjforex.brokerapi.BrokerTrade;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.connection.CredentialsFactory;
import com.jforex.dzjforex.connection.PinProvider;
import com.jforex.dzjforex.datetime.DateTimeUtils;
import com.jforex.dzjforex.datetime.ServerTime;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.dzjforex.history.HistoryHandler;
import com.jforex.dzjforex.misc.StrategyForData;
import com.jforex.dzjforex.misc.TradeCalculation;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.strategy.StrategyUtil;

public class ZorroBridge {

    private IClient client;
    private final ClientUtil clientUtil;
    private IContext context;
    private final Authentification authentification;
    private final PinProvider pinProvider;
    private final StrategyForData strategyForData;
    private long strategyID;
    private final CredentialsFactory credentialsFactory;
    private AccountInfo accountInfo;
    private TradeCalculation tradeCalculation;
    private final BrokerLogin brokerLogin;
    private BrokerAsset brokerAsset;
    private BrokerAccount brokerAccount;
    private BrokerTime brokerTime;
    private BrokerTrade brokerTrade;
    private BrokerStop brokerStop;
    private ServerTime serverTime;
    private DateTimeUtils dateTimeUtils;
    private BrokerSubscribe brokerSubscribe;
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
        brokerLogin = new BrokerLogin(client,
                                      authentification,
                                      credentialsFactory,
                                      pluginConfig);
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
        final int loginResult = brokerLogin.handle(userName,
                                                   password,
                                                   type);
        if (loginResult == ReturnCodes.LOGIN_OK) {
            strategyID = client.startStrategy(strategyForData);
            initComponents();
            final String accountID = accountInfo.id();
            accountInfos[0] = accountID;
        }

        return loginResult;
    }

    private void initComponents() {
        context = strategyForData.getContext();
        final StrategyUtil strategyUtil = strategyForData.strategyUtil();

        serverTime = new ServerTime(strategyForData, pluginConfig);
        dateTimeUtils = new DateTimeUtils(context.getDataService(), serverTime);
        accountInfo = new AccountInfo(context.getAccount(), pluginConfig);
        brokerSubscribe = new BrokerSubscribe(client, accountInfo);
        orderHandler = new OrderHandler(context,
                                        strategyUtil,
                                        pluginConfig);
        historyHandler = new HistoryHandler(context.getHistory());
        tradeCalculation = new TradeCalculation(accountInfo, strategyUtil.calculationUtil());
        brokerAsset = new BrokerAsset(accountInfo,
                                      tradeCalculation,
                                      strategyUtil);
        brokerAccount = new BrokerAccount(accountInfo);
        brokerTime = new BrokerTime(client, dateTimeUtils);
        brokerStop = new BrokerStop(orderHandler, accountInfo);
    }

    public int doLogout() {
        client.stopStrategy(strategyID);
        return brokerLogin.doLogout();
    }

    public int doBrokerTime(final double serverTimeData[]) {
        return brokerTime.handle(serverTimeData);
    }

    public int doSubscribeAsset(final String instrumentName) {
        return brokerSubscribe.handle(instrumentName);
    }

    public int doBrokerAsset(final String instrumentName,
                             final double assetParams[]) {
        return brokerAsset.handle(instrumentName, assetParams);
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        return brokerAccount.handle(accountInfoParams);
    }

    public int doBrokerBuy(final String instrumentName,
                           final double tradeParams[]) {
        ZorroLogger.log("doBrokerBuy called");
        if (!accountInfo.isTradingAllowed())
            return ReturnCodes.BROKER_BUY_FAIL;

        return orderHandler.doBrokerBuy(instrumentName, tradeParams);
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        return brokerTrade.handle(orderID, orderParams);
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        return brokerStop.handle(orderID, newSLPrice);
    }

    public int doBrokerSell(final int orderID,
                            final int amount) {
        ZorroLogger.log("doBrokerSell called");
        if (!accountInfo.isTradingAllowed())
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
