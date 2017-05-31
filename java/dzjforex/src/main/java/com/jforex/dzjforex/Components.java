package com.jforex.dzjforex;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokeraccount.BrokerAccount;
import com.jforex.dzjforex.brokerasset.BrokerAsset;
import com.jforex.dzjforex.brokerbuy.BrokerBuy;
import com.jforex.dzjforex.brokerbuy.SubmitParamsFactory;
import com.jforex.dzjforex.brokerbuy.SubmitParamsRunner;
import com.jforex.dzjforex.brokerhistory.BarFetcher;
import com.jforex.dzjforex.brokerhistory.BarHistoryByShift;
import com.jforex.dzjforex.brokerhistory.BrokerHistory;
import com.jforex.dzjforex.brokerhistory.HistoryFetchDate;
import com.jforex.dzjforex.brokerhistory.TickFetcher;
import com.jforex.dzjforex.brokerhistory.TickHistoryByShift;
import com.jforex.dzjforex.brokerlogin.BrokerLogin;
import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.brokerlogin.CredentialsFactory;
import com.jforex.dzjforex.brokerlogin.LoginRetryTimer;
import com.jforex.dzjforex.brokerlogin.PinProvider;
import com.jforex.dzjforex.brokersell.BrokerSell;
import com.jforex.dzjforex.brokersell.CloseParamsFactory;
import com.jforex.dzjforex.brokersell.CloseParamsRunner;
import com.jforex.dzjforex.brokerstop.BrokerStop;
import com.jforex.dzjforex.brokerstop.SetSLParamsFactory;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.brokersubscribe.Subscription;
import com.jforex.dzjforex.brokertime.BrokerTime;
import com.jforex.dzjforex.brokertime.NTPFetch;
import com.jforex.dzjforex.brokertime.NTPProvider;
import com.jforex.dzjforex.brokertime.NTPSynchTask;
import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.brokertime.TickTimeFetch;
import com.jforex.dzjforex.brokertime.TickTimeProvider;
import com.jforex.dzjforex.brokertime.TimeWatch;
import com.jforex.dzjforex.brokertrade.BrokerTrade;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.history.HistoryOrdersDates;
import com.jforex.dzjforex.history.HistoryOrdersProvider;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.misc.MarketState;
import com.jforex.dzjforex.misc.PriceProvider;
import com.jforex.dzjforex.order.OpenOrdersProvider;
import com.jforex.dzjforex.order.OrderIDLookUp;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderLookup;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.strategy.StrategyUtil;

public class Components {

    private PluginConfig pluginConfig;
    private IClient client;
    private ClientUtil clientUtil;
    private Zorro zorro;
    private InfoStrategy infoStrategy;
    private Clock clock;
    private BrokerLogin brokerLogin;
    private IContext context;
    private IEngine engine;
    private OrderUtil orderUtil;
    private StrategyUtil strategyUtil;
    private TickQuoteRepository tickQuoteRepository;
    private MarketState marketData;
    private ServerTimeProvider serverTimeProvider;
    private BrokerTime brokerTime;
    private AccountInfo accountInfo;
    private BrokerAsset brokerAsset;
    private BrokerAccount brokerAccount;
    private Subscription subscription;
    private BrokerSubscribe brokerSubscribe;
    private BrokerHistory brokerHistory;
    private BrokerTrade brokerTrade;
    private BrokerBuy brokerBuy;
    private BrokerSell brokerSell;
    private BrokerStop brokerStop;
    private TradeUtility tradeUtility;
    private RetryParams retryParamsForTrading;
    private OrderRepository orderRepository;
    private OrderLabelUtil orderLabelUtil;
    private OrderLookup orderLookup;
    private OpenOrdersProvider openOrdersProvider;

    public Components(final SystemComponents systemComponents) {
        initSystemComponents(systemComponents);
        initLoginComponents();
    }

    private void initSystemComponents(final SystemComponents systemComponents) {
        client = systemComponents.client();
        pluginConfig = systemComponents.pluginConfig();
        infoStrategy = systemComponents.infoStrategy();
        clientUtil = systemComponents.clientUtil();
        zorro = new Zorro(pluginConfig);
        clock = Clock.systemDefaultZone();
    }

    private void initLoginComponents() {
        final PinProvider pinProvider = new PinProvider(client, pluginConfig.realConnectURL());
        final CredentialsFactory credentialsFactory = new CredentialsFactory(pinProvider, pluginConfig);
        final LoginRetryTimer loginRetryTimer = new LoginRetryTimer(pluginConfig);
        brokerLogin = new BrokerLogin(clientUtil.authentification(),
                                      credentialsFactory,
                                      loginRetryTimer);
    }

    private void initAfterStrategyStart() {
        initStrategyComponents();
        initTimeComponents();
        initAccountComponents();
        initOrderComponents();
        initHistoryComponents();
        initTradeComponents();
    }

    private void initStrategyComponents() {
        context = infoStrategy.getContext();
        engine = context.getEngine();
        strategyUtil = infoStrategy.strategyUtil();
        orderUtil = strategyUtil.orderUtil();
        marketData = new MarketState(context.getDataService());
        tickQuoteRepository = strategyUtil
            .tickQuoteProvider()
            .repository();
    }

    private void initAccountComponents() {
        accountInfo = new AccountInfo(infoStrategy.getAccount(),
                                      strategyUtil.calculationUtil(),
                                      pluginConfig);
        brokerAccount = new BrokerAccount(accountInfo);
        subscription = new Subscription(client);
        brokerSubscribe = new BrokerSubscribe(subscription, accountInfo);
    }

    private void initTimeComponents() {
        final NTPUDPClient ntpUDPClient = new NTPUDPClient();
        final NTPFetch ntpFetch = new NTPFetch(ntpUDPClient, pluginConfig);
        final NTPSynchTask ntpSynchTask = new NTPSynchTask(ntpFetch, pluginConfig);
        final TimeWatch timeWatch = new TimeWatch(clock);
        final NTPProvider ntpProvider = new NTPProvider(ntpSynchTask,
                                                        timeWatch,
                                                        pluginConfig);
        final TickTimeFetch tickTimeFetch = new TickTimeFetch(tickQuoteRepository);
        final TickTimeProvider tickTimeProvider = new TickTimeProvider(tickTimeFetch, timeWatch);
        serverTimeProvider = new ServerTimeProvider(ntpProvider, tickTimeProvider);
        brokerTime = new BrokerTime(client,
                                    serverTimeProvider,
                                    marketData);
    }

    private void initOrderComponents() {
        orderLabelUtil = new OrderLabelUtil(clock, pluginConfig);
        orderRepository = new OrderRepository(orderLabelUtil);
        openOrdersProvider = new OpenOrdersProvider(engine, pluginConfig);
        // openOrders = new OpenOrders(openOrdersProvider, orderRepository);
    }

    private void initHistoryComponents() {
        final IHistory history = infoStrategy.getHistory();
        final HistoryWrapper historyWrapper = new HistoryWrapper(history);

        final HistoryFetchDate historyFetchDate = new HistoryFetchDate(historyWrapper, pluginConfig);
        final BarHistoryByShift barHistoryByShift = new BarHistoryByShift(historyWrapper,
                                                                          historyFetchDate,
                                                                          pluginConfig);
        final TickHistoryByShift tickHistoryByShift = new TickHistoryByShift(historyWrapper,
                                                                             historyFetchDate,
                                                                             pluginConfig);
        final BarFetcher barFetcher = new BarFetcher(barHistoryByShift);
        final TickFetcher tickFetcher = new TickFetcher(tickHistoryByShift);
        brokerHistory = new BrokerHistory(barFetcher, tickFetcher);
        final HistoryOrdersDates historyOrdersDates = new HistoryOrdersDates(serverTimeProvider, pluginConfig);
        final HistoryOrdersProvider historyOrdersProvider = new HistoryOrdersProvider(historyWrapper,
                                                                                      subscription,
                                                                                      historyOrdersDates,
                                                                                      pluginConfig);
        final OrderIDLookUp orderIDLookUpForOpenOrders = new OrderIDLookUp(openOrdersProvider.get(), orderRepository);
        final OrderIDLookUp orderIDLookUpForHistoryOrders =
                new OrderIDLookUp(historyOrdersProvider.get(), orderRepository);
        orderLookup = new OrderLookup(orderRepository,
                                      orderIDLookUpForOpenOrders,
                                      orderIDLookUpForHistoryOrders);
    }

    private void initTradeComponents() {
        final PriceProvider priceProvider = new PriceProvider(strategyUtil);
        retryParamsForTrading = retryParamsForTrading();
        tradeUtility = new TradeUtility(orderLookup,
                                        accountInfo,
                                        orderLabelUtil,
                                        pluginConfig);
        brokerAsset = new BrokerAsset(accountInfo, priceProvider);
        final CalculationUtil calculationUtil = strategyUtil.calculationUtil();
        final StopLoss stopLoss = new StopLoss(calculationUtil, pluginConfig.minPipsForSL());
        final SubmitParamsFactory orderSubmitParams = new SubmitParamsFactory(retryParamsForTrading,
                                                                              stopLoss,
                                                                              orderLabelUtil);
        final CloseParamsFactory orderCloseParams = new CloseParamsFactory(retryParamsForTrading);
        final SetSLParamsFactory orderSetSLParams = new SetSLParamsFactory(stopLoss, retryParamsForTrading);
        final SubmitParamsRunner submitParamsRunner = new SubmitParamsRunner(orderUtil, orderSubmitParams);
        final CloseParamsRunner closeParamsRunner = new CloseParamsRunner(orderUtil, orderCloseParams);
        final SetSLParamsRunner setSLParamsRunner = new SetSLParamsRunner(orderUtil, orderSetSLParams);
        brokerTrade = new BrokerTrade(tradeUtility, calculationUtil);
        brokerBuy = new BrokerBuy(submitParamsRunner,
                                  orderRepository,
                                  tradeUtility);
        brokerSell = new BrokerSell(closeParamsRunner, tradeUtility);
        brokerStop = new BrokerStop(setSLParamsRunner, tradeUtility);
    }

    private RetryParams retryParamsForTrading() {
        final RetryDelay delay = new RetryDelay(pluginConfig.orderSubmitRetryDelay(), TimeUnit.MILLISECONDS);
        return new RetryParams(pluginConfig.orderSubmitRetries(), att -> delay);
    }

    public long startAndInitStrategyComponents(final BrokerLoginData brokerLoginData) {
        client.setSubscribedInstruments(Sets.newHashSet(Instrument.EURUSD));
        final long strategyID = client.startStrategy(infoStrategy);
        initAfterStrategyStart();
        brokerLoginData.fillAccounts(accountInfo);
        return strategyID;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public IClient client() {
        return client;
    }

    public Zorro zorro() {
        return zorro;
    }

    public BrokerLogin brokerLogin() {
        return brokerLogin;
    }

    public BrokerTime brokerTime() {
        return brokerTime;
    }

    public BrokerSubscribe brokerSubscribe() {
        return brokerSubscribe;
    }

    public BrokerAsset brokerAsset() {
        return brokerAsset;
    }

    public BrokerAccount brokerAccount() {
        return brokerAccount;
    }

    public BrokerHistory brokerHistory() {
        return brokerHistory;
    }

    public BrokerTrade brokerTrade() {
        return brokerTrade;
    }

    public BrokerBuy brokerBuy() {
        return brokerBuy;
    }

    public BrokerSell brokerSell() {
        return brokerSell;
    }

    public BrokerStop brokerStop() {
        return brokerStop;
    }

    public TradeUtility tradeUtility() {
        return tradeUtility;
    }
}
