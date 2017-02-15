package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.dataprovider.AccountInfo;
import com.jforex.dzjforex.handler.LoginHandler;
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

    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);

    public ZorroBridge() {
        initClientInstance();

        clientUtil = new ClientUtil(client, pluginConfig.CACHE_DIR());
        authentification = clientUtil.authentification();
        pinProvider = new PinProvider(client, pluginConfig.CONNECT_URL_REAL());
        loginHandler = new LoginHandler(client,
                                        authentification,
                                        pinProvider,
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
        strategyID = client.startStrategy(strategyForData);
        context = strategyForData.getContext();

        accountInfo = new AccountInfo(context.getAccount());
    }

    private void fillAccountInfos(final String accountInfos[]) {
        final String accountID = accountInfo.getID();
        accountInfos[0] = accountID;
        ZorroLogger.log("Filled account infos with " + accountID);
    }

    public int doLogout() {
        return loginHandler.doLogout();
    }

    public int doBrokerTime(final double serverTime[]) {
        ZorroLogger.log("doBrokerTime called");

        return 0;
    }

    public int doBrokerAsset(final String instrumentName,
                             final double assetParams[]) {
        ZorroLogger.log("doBrokerAsset called");

        return 0;
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        ZorroLogger.log("doBrokerAccount called");

        return 0;
    }

    public int doBrokerBuy(final String instrumentName,
                           final double tradeParams[]) {
        ZorroLogger.log("doBrokerBuy called");

        return 0;
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        ZorroLogger.log("doBrokerTrade called");

        return 0;
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        ZorroLogger.log("doBrokerStop called");

        return 0;
    }

    public int doBrokerSell(final int orderID,
                            final int amount) {
        ZorroLogger.log("doBrokerSell called");

        return 0;
    }

    public int doBrokerHistory2(final String instrumentName,
                                final double startDate,
                                final double endDate,
                                final int tickMinutes,
                                final int nTicks,
                                final double tickParams[]) {
        ZorroLogger.log("doBrokerHistory2 called");

        return 0;
    }

    public int doHistoryDownload() {
        ZorroLogger.log("doHistoryDownload called");

        return 0;
    }
}
