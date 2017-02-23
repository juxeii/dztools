package com.jforex.dzjforex.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

import io.reactivex.Observable;

public class LoginHandler {

    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final Authentification authentification,
                        final CredentialsFactory credentialsFactory) {
        this.authentification = authentification;
        this.credentialsFactory = credentialsFactory;
    }

    public int login(final String username,
                     final String password,
                     final String loginType) {
        final LoginCredentials credentials = credentialsFactory.create(username,
                                                                       password,
                                                                       loginType);
        return loginWithCredentials(credentials);
    }

    private int loginWithCredentials(final LoginCredentials credentials) {
        return authentification
            .login(credentials)
            .andThen(Observable.just(ZorroReturnValues.LOGIN_OK.getValue()))
            .onErrorResumeNext(err -> {
                logger.error("Failed to login with exception " + err.getMessage());
                return Observable.just(ZorroReturnValues.LOGIN_FAIL.getValue());
            })
            .blockingLast();
    }

    public int logout() {
        authentification
            .logout()
            .blockingAwait();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }
}
