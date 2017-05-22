package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokeraccount.BrokerAccount;
import com.jforex.dzjforex.brokeraccount.BrokerAccountData;
import com.jforex.dzjforex.brokerasset.BrokerAsset;
import com.jforex.dzjforex.brokerasset.BrokerAssetData;
import com.jforex.dzjforex.brokerbuy.BrokerBuy;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerhistory.BrokerHistory;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
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

    private final Components components;
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
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        components = new Components(pluginConfig);
        zorro = components.zorro();
        brokerLogin = components.brokerLogin();
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

    public int doLogin(final String User,
                       final String Pwd,
                       final String Type,
                       final String Accounts[]) {
        final BrokerLoginData brokerLoginData = new BrokerLoginData(User,
                                                                    Pwd,
                                                                    Type,
                                                                    Accounts);
        final Single<Integer> loginTask = brokerLogin
            .login(brokerLoginData)
            .doOnSuccess(loginOK -> initComponents(brokerLoginData));

        return zorro.progressWait(loginTask);
    }

    public int doLogout() {
//        client.stopStrategy(strategyID);
//        return brokerLogin
//            .logout()
//            .blockingGet();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        final BrokerTimeData brokerTimeData = new BrokerTimeData(pTimeUTC);
        return brokerTime.get(brokerTimeData);
    }

    public int doSubscribeAsset(final String Asset) {
        return brokerSubscribe
            .forName(Asset)
            .blockingGet();
    }

    public int doBrokerAsset(final String Asset,
                             final double assetParams[]) {
        final BrokerAssetData brokerAssetData = new BrokerAssetData(Asset, assetParams);
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

    public int doBrokerTrade(final int nTradeID,
                             final double tradeParams[]) {
        final BrokerTradeData brokerTradeData = new BrokerTradeData(nTradeID, tradeParams);
        return brokerTrade
            .fillParams(brokerTradeData)
            .blockingGet();
    }

    public int doBrokerBuy(final String Asset,
                           final int nAmount,
                           final double dStopDist,
                           final double tradeParams[]) {
        final double amount = tradeUtility.contractsToAmount(nAmount);
        final OrderCommand orderCommand = tradeUtility.orderCommandForContracts(nAmount);
        final BrokerBuyData brokerBuyData = new BrokerBuyData(Asset,
                                                              amount,
                                                              orderCommand,
                                                              dStopDist,
                                                              tradeParams);
        return brokerBuy
            .openTrade(brokerBuyData)
            .blockingGet();
    }

    public int doBrokerSell(final int nTradeID,
                            final int nAmount) {
        final double amount = tradeUtility.contractsToAmount(nAmount);
        final BrokerSellData brokerSellData = new BrokerSellData(nTradeID, amount);

        return brokerSell
            .closeTrade(brokerSellData)
            .blockingGet();
    }

    public int doBrokerStop(final int nTradeID,
                            final double dStop) {
        final BrokerStopData brokerStopData = new BrokerStopData(nTradeID, dStop);
        return brokerStop
            .setSL(brokerStopData)
            .blockingGet();
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
        return zorro.progressWait(brokerHistory.get(brokerHistoryData));
    }

    public int doSetOrderText(final String orderText) {
        Zorro.logDiagnose("doSetOrderText for " + orderText + " called but not yet supported!");
        return ZorroReturnValues.BROKER_COMMAND_OK.getValue();
    }
}
