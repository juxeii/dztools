package com.jforex.dzjforex.brokerlogin;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class BrokerLogin {

    private final IClient client;
    private final LoginExecutor loginExecutor;
    private final Observable<Long> retryDelayTimer;
    private final BehaviorSubject<Boolean> isLoginAvailable = BehaviorSubject.createDefault(true);

    private final static Logger logger = LogManager.getLogger(BrokerLogin.class);

    public BrokerLogin(final IClient client,
                       final LoginExecutor loginExecutor,
                       final PluginConfig pluginConfig) {
        this.client = client;
        this.loginExecutor = loginExecutor;

        retryDelayTimer = Observable
            .timer(pluginConfig.loginRetryDelay(), TimeUnit.MILLISECONDS)
            .doOnSubscribe(d -> {
                isLoginAvailable.onNext(false);
                logger.debug("Starting login retry delay timer. Login is not available until timer elapsed.");
            })
            .doOnComplete(() -> {
                isLoginAvailable.onNext(true);
                logger.debug("Login retry delay timer completed. Login is available again.");
            });
    }

    public Completable login(final BrokerLoginData brokerLoginData) {
        if (client.isConnected())
            return Completable.complete();
        if (!isLoginAvailable.getValue())
            return Completable.error(new JFException("No login while retry timer running!"));

        return loginExecutor
            .login(brokerLoginData)
            .doOnError(e -> {
                logger.error("Failed to login! " + e.getMessage());
                retryDelayTimer.subscribe();
            });
    }

    public int logout() {
        return loginExecutor
            .logout()
            .toSingleDefault(ZorroReturnValues.LOGOUT_OK.getValue())
            .blockingGet();
    }
}
