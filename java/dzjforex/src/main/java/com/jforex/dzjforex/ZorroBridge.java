package com.jforex.dzjforex;

import com.jforex.dzjforex.zorro.KZorroBridge;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ZorroBridge {

    private final KZorroBridge kBridge = new KZorroBridge();
    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public int doLogin(final String username,
                       final String password,
                       final String accountType,
                       final String Accounts[]) {
        return kBridge.login(username,
                password,
                accountType,
                Accounts);
    }

    public int doLogout() {
        return kBridge.logout();
    }

    public int doBrokerTime(final double pTimeUTC[]) {
        return kBridge.brokerTime(pTimeUTC);
    }

    public int doSubscribeAsset(final String assetName) {
        return kBridge.subscribe(assetName);
    }

    public int doBrokerAsset(final String assetName,
                             final double assetParams[]) {
        return kBridge.brokerAsset(assetName, assetParams);
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        return kBridge.brokerAccount(accountInfoParams);
    }

    public int doBrokerTrade(final int orderID,
                             final double tradeParams[]) {
        return kBridge.brokerTrade(orderID, tradeParams);
    }

    public int doBrokerBuy2(final String assetName,
                            final int contracts,
                            final double slDistance,
                            final double limit,
                            final double tradeParams[]) {
        return kBridge.brokerBuy(assetName,
                contracts,
                slDistance,
                limit,
                tradeParams);
    }

    public int doBrokerSell(final int orderID,
                            final int contracts) {
        return kBridge.brokerSell(orderID, contracts);
    }

    public int doBrokerStop(final int orderID,
                            final double slPrice) {
        return kBridge.brokerStop(orderID, slPrice);
    }

    public int doBrokerHistory2(final String assetName,
                                final double utcStartDate,
                                final double utcEndDate,
                                final int periodInMinutes,
                                final int noOfTicks,
                                final double tickParams[]) {
        return kBridge.brokerHistory(assetName,
                utcStartDate,
                utcEndDate,
                periodInMinutes,
                noOfTicks,
                tickParams);
    }

    public int doSetOrderText(final String orderText) {
        return 42;
    }

    public static native int jcallback_BrokerError(String errorMsg);

    public static native int jcallback_BrokerProgress(int progress);
}
