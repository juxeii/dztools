package com.jforex.dzjforex;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.login.BrokerLogin;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZorroBridge {

    private final IClient client;
    private final BrokerLogin brokerLogin;
    private long strategyID;

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        client = getClient();
        brokerLogin = new BrokerLogin();
    }

    private IClient getClient() {
        return Single
                .fromCallable(ClientFactory::getDefaultInstance)
                .doOnError(e -> logger.error("Error retrieving IClient instance! " + e.getMessage()))
                .blockingGet();
    }

    public int doLogin(final String username,
                       final String password,
                       final String accountType,
                       final String Accounts[]) {
        logger.debug("WOOOOOOOOOOOOOOOOO");
        int result = brokerLogin.login(client);
        logger.debug("Login result is " + result);
        return result;
    }

    public int doLogout() {
        return 42;

    }

    public int doBrokerTime(final double pTimeUTC[]) {
        return 42;

    }

    public int doSubscribeAsset(final String assetName) {
        return 42;

    }

    public int doBrokerAsset(final String assetName,
                             final double assetParams[]) {
        return 42;
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        return 42;
    }

    public int doBrokerTrade(final int orderID,
                             final double tradeParams[]) {
        return 42;
    }

    public int doBrokerBuy(final String assetName,
                           final int contracts,
                           final double slDistance,
                           final double tradeParams[]) {
        return 42;
    }

    public int doBrokerSell(final int orderID,
                            final int contracts) {
        return 42;
    }

    public int doBrokerStop(final int orderID,
                            final double slPrice) {
        return 42;

    }

    public int doBrokerHistory2(final String assetName,
                                final double utcStartDate,
                                final double utcEndDate,
                                final int periodInMinutes,
                                final int noOfTicks,
                                final double tickParams[]) {
        return 42;

    }

    public int doSetOrderText(final String orderText) {
        return 42;
    }
}
