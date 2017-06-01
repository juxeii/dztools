package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokeraccount.BrokerAccount;
import com.jforex.dzjforex.brokeraccount.BrokerAccountData;
import com.jforex.dzjforex.brokerasset.BrokerAsset;
import com.jforex.dzjforex.brokerasset.BrokerAssetData;
import com.jforex.dzjforex.brokerbuy.BrokerBuy;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerhistory.BrokerHistory;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.brokerhistory.HistoryTickFiller;
import com.jforex.dzjforex.brokerlogin.BrokerLogin;
import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.brokersell.BrokerSell;
import com.jforex.dzjforex.brokersell.BrokerSellData;
import com.jforex.dzjforex.brokerstop.BrokerStop;
import com.jforex.dzjforex.brokerstop.BrokerStopData;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.brokertime.BrokerTime;
import com.jforex.dzjforex.brokertime.BrokerTimeData;
import com.jforex.dzjforex.brokertrade.BrokerTrade;
import com.jforex.dzjforex.brokertrade.BrokerTradeData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class ZorroBridge {

    private final SystemComponents systemComponents;
    private final Components components;
    private final IClient client;
    private final Zorro zorro;
    private final BrokerLogin brokerLogin;
    private BrokerTime brokerTime;
    private BrokerSubscribe brokerSubscribe;
    private BrokerAsset brokerAsset;
    private BrokerAccount brokerAccount;
    private BrokerTrade brokerTrade;
    private BrokerBuy brokerBuy;
    private BrokerSell brokerSell;
    private BrokerStop brokerStop;
    private BrokerHistory brokerHistory;
    private TradeUtility tradeUtility;
    private long strategyID;

    private final static PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);
    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        client = getClient();
        systemComponents = new SystemComponents(client, pluginConfig);
        components = new Components(systemComponents);
        zorro = components.zorro();
        brokerLogin = components.brokerLogin();
    }

    private IClient getClient() {
        return Single
            .fromCallable(ClientFactory::getDefaultInstance)
            .doOnError(e -> logger.error("Error retrieving IClient instance! " + e.getMessage()))
            .blockingGet();
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
        tradeUtility = components.tradeUtility();
    }

    public int doLogin(final String username,
                       final String password,
                       final String accountType,
                       final String Accounts[]) {
        final BrokerLoginData brokerLoginData = new BrokerLoginData(username,
                                                                    password,
                                                                    accountType,
                                                                    Accounts);
        final Single<Integer> loginTask = brokerLogin
            .login(brokerLoginData)
            .doOnSuccess(loginOK -> initComponents(brokerLoginData));

        return zorro.progressWait(loginTask);
    }

    public int doLogout() {
        client.stopStrategy(strategyID);
        return brokerLogin
            .logout()
            .blockingGet();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        final BrokerTimeData brokerTimeData = new BrokerTimeData(pTimeUTC);
        return brokerTime
            .get(brokerTimeData)
            .blockingGet();
    }

    public int doSubscribeAsset(final String assetName) {
        return brokerSubscribe
            .forName(assetName)
            .blockingGet();
    }

    public int doBrokerAsset(final String assetName,
                             final double assetParams[]) {
        final BrokerAssetData brokerAssetData = new BrokerAssetData(assetName, assetParams);
        return brokerAsset
            .fillParams(brokerAssetData)
            .blockingGet();
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        final BrokerAccountData brokerAccountData = new BrokerAccountData(accountInfoParams);
        return brokerAccount
            .handle(brokerAccountData)
            .blockingGet();
    }

    public int doBrokerTrade(final int orderID,
                             final double tradeParams[]) {
        final BrokerTradeData brokerTradeData = new BrokerTradeData(orderID, tradeParams);
        return brokerTrade
            .fillParams(brokerTradeData)
            .blockingGet();
    }

    public int doBrokerBuy(final String assetName,
                           final int contracts,
                           final double slDistance,
                           final double tradeParams[]) {
        final double amount = tradeUtility.contractsToAmount(contracts);
        final OrderCommand orderCommand = tradeUtility.orderCommandForContracts(contracts);
        final BrokerBuyData brokerBuyData = new BrokerBuyData(assetName,
                                                              amount,
                                                              orderCommand,
                                                              slDistance,
                                                              tradeParams);
        return brokerBuy
            .openTrade(brokerBuyData)
            .blockingGet();
    }

    public int doBrokerSell(final int orderID,
                            final int contracts) {
        final double amount = tradeUtility.contractsToAmount(contracts);
        final BrokerSellData brokerSellData = new BrokerSellData(orderID, amount);
        return brokerSell
            .closeTrade(brokerSellData)
            .blockingGet();
    }

    public int doBrokerStop(final int orderID,
                            final double slPrice) {
        final BrokerStopData brokerStopData = new BrokerStopData(orderID, slPrice);
        return brokerStop
            .setSL(brokerStopData)
            .blockingGet();
    }

    public int doBrokerHistory2(final String assetName,
                                final double utcStartDate,
                                final double utcEndDate,
                                final int periodInMinutes,
                                final int noOfTicks,
                                final double tickParams[]) {
        final HistoryTickFiller historyTickFiller = new HistoryTickFiller(tickParams);
        final BrokerHistoryData brokerHistoryData = new BrokerHistoryData(assetName,
                                                                          utcStartDate,
                                                                          utcEndDate,
                                                                          periodInMinutes,
                                                                          noOfTicks,
                                                                          historyTickFiller);
        return zorro.progressWait(brokerHistory.get(brokerHistoryData));
    }

    public int doSetOrderText(final String orderText) {
        Zorro.logDiagnose("doSetOrderText for " + orderText + " called but not yet supported!");
        return ZorroReturnValues.BROKER_COMMAND_OK.getValue();
    }
}
