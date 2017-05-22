package com.jforex.dzjforex.brokerlogin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.programming.connection.Authentification;

import io.reactivex.Completable;
import io.reactivex.Single;

public class BrokerLogin {

    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;
    private final LoginRetryTimer loginRetryTimer;

    private final static Logger logger = LogManager.getLogger(BrokerLogin.class);

    public BrokerLogin(final Authentification authentification,
                       final CredentialsFactory credentialsFactory,
                       final LoginRetryTimer loginRetryTimer) {
        this.authentification = authentification;
        this.credentialsFactory = credentialsFactory;
        this.loginRetryTimer = loginRetryTimer;
    }

    public Single<Integer> login(final BrokerLoginData brokerLoginData) {
        return Single.defer(() -> !loginRetryTimer.isLoginPermitted()
                ? Single.just(ZorroReturnValues.LOGIN_FAIL.getValue())
                : loginTask(brokerLoginData));
    }

    private Single<Integer> loginTask(final BrokerLoginData brokerLoginData) {
        return Single
            .just(credentialsFactory.create(brokerLoginData))
            .flatMapCompletable(authentification::login)
            .toSingleDefault(ZorroReturnValues.LOGIN_OK.getValue())
            .doOnError(e -> {
                logger.error("Failed to login! " + e.getMessage());
                loginRetryTimer.start();
            })
            .onErrorReturnItem(ZorroReturnValues.LOGIN_FAIL.getValue());
    }

    public Single<Integer> logout() {
        return Completable
            .defer(() -> authentification.logout())
            .toSingleDefault(ZorroReturnValues.LOGOUT_OK.getValue());
    }
}
