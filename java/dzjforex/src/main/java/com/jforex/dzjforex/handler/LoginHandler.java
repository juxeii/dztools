package com.jforex.dzjforex.handler;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerLoginData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.misc.PinProvider;
import com.jforex.programming.client.ClientUtil;

import io.reactivex.Observable;

public class LoginHandler {

    private final LoginExecutor loginExecutor;
    private final IClient client;
    private Observable<Long> retryDelayTimer;
    private boolean isLoginAvailable = true;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final SystemHandler systemHandler) {
        final ClientUtil clientUtil = systemHandler.clientUtil();
        final PluginConfig pluginConfig = systemHandler.pluginConfig();
        final PinProvider pinProvider = new PinProvider(systemHandler);
        final CredentialsFactory credentialsFactory = new CredentialsFactory(pinProvider, pluginConfig);
        client = clientUtil.client();
        loginExecutor = new LoginExecutor(clientUtil.authentification(),
                                          credentialsFactory,
                                          systemHandler.zorro());

        initRetryDelayTimer(pluginConfig.loginRetryDelay());
    }

    private void initRetryDelayTimer(final long retryDelay) {
        retryDelayTimer = Observable
            .timer(retryDelay, TimeUnit.MILLISECONDS)
            .doOnSubscribe(d -> {
                isLoginAvailable = false;
                logger.debug("Starting login retry delay timer. Login is not available until timer elapsed.");
            })
            .doOnComplete(() -> {
                isLoginAvailable = true;
                logger.debug("Login retry delay timer completed. Login is available again.");
            });
    }

    public int login(final BrokerLoginData brokerLoginData) {
        if (client.isConnected())
            return ZorroReturnValues.LOGIN_OK.getValue();
        if (!isLoginAvailable)
            return ZorroReturnValues.LOGIN_FAIL.getValue();

        return handleLoginResult(loginExecutor.login(brokerLoginData));
    }

    private int handleLoginResult(final int loginResult) {
        if (loginResult == ZorroReturnValues.LOGIN_FAIL.getValue())
            startRetryDelayTimer();
        return loginResult;
    }

    private void startRetryDelayTimer() {
        retryDelayTimer.subscribe();
    }

    public int logout() {
        return loginExecutor.logout();
    }
}
