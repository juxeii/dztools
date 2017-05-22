package com.jforex.dzjforex;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;

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
import com.jforex.dzjforex.brokerhistory.BrokerHistory;
import com.jforex.dzjforex.brokerhistory.TickFetcher;
import com.jforex.dzjforex.brokerlogin.BrokerLogin;
import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.brokerlogin.CredentialsFactory;
import com.jforex.dzjforex.brokerlogin.LoginRetryTimer;
import com.jforex.dzjforex.brokerlogin.PinProvider;
import com.jforex.dzjforex.brokersell.BrokerSell;
import com.jforex.dzjforex.brokersell.CloseParamsRunner;
import com.jforex.dzjforex.brokersell.OrderCloseParams;
import com.jforex.dzjforex.brokerstop.BrokerStop;
import com.jforex.dzjforex.brokerstop.OrderSetSLParams;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.brokertime.BrokerTime;
import com.jforex.dzjforex.brokertrade.BrokerTrade;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.history.HistoryOrders;
import com.jforex.dzjforex.history.HistoryOrdersProvider;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.history.HistoryUtility;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.misc.ClientProvider;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.misc.MarketState;
import com.jforex.dzjforex.misc.PriceProvider;
import com.jforex.dzjforex.order.OpenOrders;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderLookup;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.dzjforex.time.NTPFetch;
import com.jforex.dzjforex.time.NTPProvider;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.dzjforex.time.TickTimeProvider;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.strategy.StrategyUtil;

public class Components {

    private final PluginConfig pluginConfig;
    private final IClient client;
    private final Zorro zorro;
    private final InfoStrategy infoStrategy;
    private final Clock clock;
    private final BrokerLogin brokerLogin;
    private OrderUtil orderUtil;
    private ServerTimeProvider serverTimeProvider;
    private BrokerTime brokerTime;
    private AccountInfo accountInfo;
    private BrokerAsset brokerAsset;
    private BrokerAccount brokerAccount;
    private BrokerSubscribe brokerSubscribe;
    private BrokerHistory brokerHistory;
    private HistoryProvider historyProvider;
    private BrokerTrade brokerTrade;
    private BrokerBuy brokerBuy;
    private BrokerSell brokerSell;
    private BrokerStop brokerStop;
    private TradeUtility tradeUtility;
    private final RetryParams retryParamsForTrading;

    public Components(final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;

        client = ClientProvider.get();
        final ClientUtil clientUtil = new ClientUtil(client, pluginConfig.cacheDirectory());
        zorro = new Zorro(pluginConfig);
        infoStrategy = new InfoStrategy();
        clock = Clock.systemDefaultZone();
        final PinProvider pinProvider = new PinProvider(client, pluginConfig().realConnectURL());
        final CredentialsFactory credentialsFactory = new CredentialsFactory(pinProvider, pluginConfig);
        final LoginRetryTimer loginRetryTimer = new LoginRetryTimer(pluginConfig);
        brokerLogin = new BrokerLogin(clientUtil.authentification(),
                                      credentialsFactory,
                                      loginRetryTimer);
        retryParamsForTrading = retryParamsForTrading();
    }

    private RetryParams retryParamsForTrading() {
        final RetryDelay delay = new RetryDelay(pluginConfig.orderSubmitRetryDelay(), TimeUnit.MILLISECONDS);
        return new RetryParams(pluginConfig.orderSubmitRetries(), att -> delay);
    }

    private void initAfterStrategyStart() {
        final NTPUDPClient ntpUDPClient = new NTPUDPClient();
        final NTPFetch ntpFetch = new NTPFetch(ntpUDPClient, pluginConfig);
        final NTPProvider ntpProvider = new NTPProvider(ntpFetch, pluginConfig);
        final MarketState marketData = new MarketState(infoStrategy
            .getContext()
            .getDataService());
        final StrategyUtil strategyUtil = infoStrategy.strategyUtil();
        final TickQuoteRepository tickQuoteRepository = strategyUtil
            .tickQuoteProvider()
            .repository();
        final TickTimeProvider tickTimeProvider = new TickTimeProvider(tickQuoteRepository, clock);
        serverTimeProvider = new ServerTimeProvider(ntpProvider,
                                                    tickTimeProvider,
                                                    clock);
        brokerTime = new BrokerTime(client,
                                    serverTimeProvider,
                                    marketData);
        accountInfo = new AccountInfo(infoStrategy.getAccount(),
                                      strategyUtil.calculationUtil(),
                                      pluginConfig);
        brokerAccount = new BrokerAccount(accountInfo);

        brokerSubscribe = new BrokerSubscribe(client, accountInfo);
        final IHistory history = infoStrategy.getHistory();
        final HistoryWrapper historyWrapper = new HistoryWrapper(history);
        final HistoryUtility historyUtility = new HistoryUtility(historyWrapper, pluginConfig);
        historyProvider = new HistoryProvider(historyUtility, pluginConfig);
        final BarFetcher barFetcher = new BarFetcher(historyProvider);
        final TickFetcher tickFetcher = new TickFetcher(historyProvider);
        brokerHistory = new BrokerHistory(barFetcher, tickFetcher);
        final IEngine engine = infoStrategy
            .getContext()
            .getEngine();
        final OrderLabelUtil orderLabelUtil = new OrderLabelUtil(pluginConfig, clock);
        final OrderRepository orderRepository = new OrderRepository(orderLabelUtil);
        final OpenOrders openOrders = new OpenOrders(engine, orderRepository);
        final HistoryOrdersProvider historyOrdersProvider = new HistoryOrdersProvider(historyWrapper,
                                                                                      brokerSubscribe,
                                                                                      pluginConfig,
                                                                                      serverTimeProvider);
        final HistoryOrders historyOrders = new HistoryOrders(historyOrdersProvider, orderRepository);

        final OrderLookup orderLookup = new OrderLookup(orderRepository,
                                                        openOrders,
                                                        historyOrders);
        final PriceProvider priceProvider = new PriceProvider(strategyUtil);
        tradeUtility = new TradeUtility(orderLookup,
                                        priceProvider,
                                        accountInfo,
                                        orderLabelUtil,
                                        retryParamsForTrading,
                                        pluginConfig);
        brokerAsset = new BrokerAsset(accountInfo, priceProvider);
        final StopLoss stopLoss = new StopLoss(tradeUtility, pluginConfig.minPipsForSL());
        final SubmitParamsFactory orderSubmitParams = new SubmitParamsFactory(retryParamsForTrading,
                                                                              stopLoss,
                                                                              orderLabelUtil);
        final OrderCloseParams orderCloseParams = new OrderCloseParams(tradeUtility);
        final OrderSetSLParams orderSetSLParams = new OrderSetSLParams(stopLoss, retryParamsForTrading);
        orderUtil = strategyUtil.orderUtil();
        final SubmitParamsRunner submitParamsRunner = new SubmitParamsRunner(orderUtil, orderSubmitParams);
        final CloseParamsRunner closeParamsRunner = new CloseParamsRunner(orderUtil, orderCloseParams);
        final SetSLParamsRunner setSLParamsRunner = new SetSLParamsRunner(orderUtil, orderSetSLParams);
        brokerTrade = new BrokerTrade(tradeUtility);
        brokerBuy = new BrokerBuy(submitParamsRunner,
                                  orderRepository,
                                  tradeUtility);
        brokerSell = new BrokerSell(closeParamsRunner, tradeUtility);
        brokerStop = new BrokerStop(setSLParamsRunner, tradeUtility);
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

    public BrokerSubscribe subscribeAsset() {
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
