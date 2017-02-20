package com.jforex.dzjforex.brokerapi;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.handler.LoginHandler;

import io.reactivex.Observable;

public class BrokerLogin {

    private final LoginHandler loginHandler;
    private final IClient client;
    private Observable<Long> retryDelayTimer;
    private boolean isLoginAvailable = true;

    private final static Logger logger = LogManager.getLogger(BrokerLogin.class);

    public BrokerLogin(final LoginHandler loginHandler,
                       final IClient client,
                       final PluginConfig pluginConfigMock) {
        this.loginHandler = loginHandler;
        this.client = client;

        initRetryDelayTimer(pluginConfigMock.loginRetryDelay());
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

    public int login(final String username,
                     final String password,
                     final String loginType) {
        if (client.isConnected())
            return Constant.LOGIN_OK;
        if (!isLoginAvailable)
            return Constant.LOGIN_FAIL;

        return handleLoginResult(loginHandler.login(username,
                                                    password,
                                                    loginType));
    }

    private int handleLoginResult(final int loginResult) {
        if (loginResult == Constant.LOGIN_FAIL)
            startRetryDelayTimer();
        return loginResult;
    }

    private void startRetryDelayTimer() {
        retryDelayTimer.subscribe();
    }

    public int logout() {
        return loginHandler.logout();
    }
}
