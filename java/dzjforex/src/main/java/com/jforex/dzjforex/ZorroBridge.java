package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerAccount;
import com.jforex.dzjforex.brokerapi.BrokerAsset;
import com.jforex.dzjforex.brokerapi.BrokerBuy;
import com.jforex.dzjforex.brokerapi.BrokerLogin;
import com.jforex.dzjforex.brokerapi.BrokerSell;
import com.jforex.dzjforex.brokerapi.BrokerStop;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.brokerapi.BrokerTime;
import com.jforex.dzjforex.brokerapi.BrokerTrade;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.dzjforex.history.BrokerHistory2;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.misc.PinProvider;
import com.jforex.dzjforex.misc.TradeCalculation;
import com.jforex.dzjforex.time.DateTimeUtils;
import com.jforex.dzjforex.time.NTPFetch;
import com.jforex.dzjforex.time.NTPProvider;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.dzjforex.time.TickTimeProvider;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class ZorroBridge {

    private IClient client;
    private final ClientUtil clientUtil;
    private IContext context;
    private final PinProvider pinProvider;
    private final InfoStrategy infoStrategy;
    private long strategyID;
    private final CredentialsFactory credentialsFactory;
    private AccountInfo accountInfo;
    private TradeCalculation tradeCalculation;
    private final BrokerLogin brokerLogin;
    private HistoryProvider historyProvider;
    private BrokerAsset brokerAsset;
    private BrokerAccount brokerAccount;
    private BrokerTime brokerTime;
    private BrokerTrade brokerTrade;
    private BrokerStop brokerStop;
    private BrokerBuy brokerBuy;
    private BrokerSell brokerSell;
    private DateTimeUtils dateTimeUtils;
    private BrokerSubscribe brokerSubscribe;
    private OrderHandler orderHandler;
    private BrokerHistory2 brokerHistory2;
    private NTPFetch ntpFetch;
    private NTPProvider ntpProvider;
    private ServerTimeProvider serverTimeProvider;
    private TickTimeProvider tickTimeProvider;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        initClientInstance();

        clientUtil = new ClientUtil(client, pluginConfig.CACHE_DIR());
        pinProvider = new PinProvider(client, pluginConfig.CONNECT_URL_REAL());
        credentialsFactory = new CredentialsFactory(pinProvider, pluginConfig);
        brokerLogin = new BrokerLogin(clientUtil, credentialsFactory);
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
        ZorroLogger.indicateError();
    }

    private void initComponents() {
        logger.info("initComponents()");
        context = infoStrategy.getContext();
        final StrategyUtil strategyUtil = infoStrategy.strategyUtil();

        dateTimeUtils = new DateTimeUtils(context.getDataService());
        accountInfo = new AccountInfo(context.getAccount(), pluginConfig);
        brokerSubscribe = new BrokerSubscribe(client, accountInfo);
        orderHandler = new OrderHandler(context,
                                        strategyUtil,
                                        pluginConfig);
        historyProvider = new HistoryProvider(context.getHistory());
        brokerHistory2 = new BrokerHistory2(historyProvider);
        tradeCalculation = new TradeCalculation(accountInfo, strategyUtil.calculationUtil());
        brokerAsset = new BrokerAsset(accountInfo,
                                      tradeCalculation,
                                      strategyUtil);
        brokerAccount = new BrokerAccount(accountInfo);

        ntpFetch = new NTPFetch(pluginConfig);
        ntpProvider = new NTPProvider(ntpFetch, pluginConfig);
        tickTimeProvider = new TickTimeProvider(strategyUtil.tickQuoteProvider().repository());
        serverTimeProvider = new ServerTimeProvider(ntpProvider, tickTimeProvider);
        brokerTime = new BrokerTime(client,
                                    serverTimeProvider,
                                    dateTimeUtils);
        brokerTrade = new BrokerTrade(orderHandler, strategyUtil);
        brokerStop = new BrokerStop(strategyUtil,
                                    orderHandler,
                                    accountInfo);
        brokerBuy = new BrokerBuy(strategyUtil,
                                  orderHandler,
                                  accountInfo,
                                  pluginConfig);
        brokerSell = new BrokerSell(strategyUtil,
                                    orderHandler,
                                    accountInfo,
                                    pluginConfig);
    }

    public int doLogin(final String userName,
                       final String password,
                       final String type,
                       final String accountInfos[]) {
        final int loginResult = brokerLogin.doLogin(userName,
                                                    password,
                                                    type);
        if (loginResult == ReturnCodes.LOGIN_OK) {
            strategyID = client.startStrategy(infoStrategy);
            initComponents();
            accountInfos[0] = accountInfo.id();
        }

        return loginResult;
    }

    public int doLogout() {
        client.stopStrategy(strategyID);
        return brokerLogin.doLogout();
    }

    public int doBrokerTime(final double serverTimeData[]) {
        return brokerTime.doBrokerTime(serverTimeData);
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
        return brokerBuy.doBrokerBuy(instrumentName, tradeParams);
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        return brokerTrade.fillTradeParams(orderID, orderParams);
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        ZorroLogger.log("doBrokerStop called");
        return brokerStop.setSL(orderID, newSLPrice);
    }

    public int doBrokerSell(final int nTradeID,
                            final int nAmount) {
        return brokerSell.closeTrade(nTradeID, nAmount);
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

        return brokerHistory2.handle(instrumentName,
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

        return brokerHistory2.doHistoryDownload();
    }

    public int doSetOrderText(final String orderText) {
        ZorroLogger.log("doSetOrderText for " + orderText + " called but not yet supported!");
        return ReturnCodes.BROKER_COMMAND_OK;
    }
}
