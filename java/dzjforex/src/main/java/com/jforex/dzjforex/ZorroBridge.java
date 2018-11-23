package com.jforex.dzjforex;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.login.BrokerLogin;
import com.jforex.dzjforex.login.LoginData;
import com.jforex.dzjforex.misc.FunctionsKt;
import com.jforex.dzjforex.misc.PluginStrategy;
import com.jforex.dzjforex.misc.ZorroCommunication;
import com.jforex.dzjforex.settings.PluginSettings;
import io.reactivex.Single;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZorroBridge {

    private final IClient client;
    private final BrokerLogin brokerLogin;
    private final ZorroCommunication zCommunication;
    private final PluginStrategy pluginStrategy;

    private final static PluginSettings pluginSettings = ConfigFactory.create(PluginSettings.class);
    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        client = FunctionsKt.getClient();
        zCommunication = new ZorroCommunication(pluginSettings);
        brokerLogin = new BrokerLogin(client);
        pluginStrategy = new PluginStrategy(client);
    }

    public int doLogin(final String username,
                       final String password,
                       final String accountType,
                       final String Accounts[]) {
        LoginData loginData = new LoginData(
                username,
                password,
                accountType);
        Single<Integer> loginTask = brokerLogin
                .login(loginData)
                .doOnSuccess(loginOK -> pluginStrategy.start(Accounts));

        return zCommunication.progressWait(loginTask);
    }

    public int doLogout() {
        pluginStrategy.stop();
        return brokerLogin
                .logout()
                .blockingGet();
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
