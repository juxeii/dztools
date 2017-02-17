package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

import io.reactivex.Observable;

public class BrokerLogin {

    private final IClient client;
    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;

    private final static Logger logger = LogManager.getLogger(BrokerLogin.class);

    public BrokerLogin(final ClientUtil clientUtil,
                       final CredentialsFactory credentialsFactory) {
        this.client = clientUtil.client();
        this.authentification = clientUtil.authentification();
        this.credentialsFactory = credentialsFactory;
    }

    public int doLogin(final String userName,
                       final String password,
                       final String loginType) {
        if (client.isConnected())
            return ReturnCodes.LOGIN_OK;

        final LoginCredentials credentials = credentialsFactory.create(userName,
                                                                       password,
                                                                       loginType);
        return login(credentials);
    }

    private int login(final LoginCredentials credentials) {
        return authentification
            .login(credentials)
            .andThen(Observable.just(ReturnCodes.LOGIN_OK))
            .onErrorResumeNext(err -> {
                logger.error("Failed to login with exception " + err.getMessage());
                return Observable.just(ReturnCodes.LOGIN_FAIL);
            })
            .blockingLast();
    }

    public int doLogout() {
        authentification
            .logout()
            .blockingAwait();
        return ReturnCodes.LOGOUT_OK;
    }
}
