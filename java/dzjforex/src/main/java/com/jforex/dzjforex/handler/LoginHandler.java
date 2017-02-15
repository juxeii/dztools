package com.jforex.dzjforex.handler;

import java.util.concurrent.TimeUnit;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.settings.ReturnCodes;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

public class LoginHandler {

    private final IClient client;
    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;

    public LoginHandler(final IClient client,
                        final Authentification authentification,
                        final CredentialsFactory credentialsFactory) {
        this.client = client;
        this.authentification = authentification;
        this.credentialsFactory = credentialsFactory;
    }

    public int doLogin(final String userName,
                       final String password,
                       final String loginType) {
        final LoginCredentials credentials = credentialsFactory.create(userName,
                                                                       password,
                                                                       loginType);
        return login(credentials);
    }

    private int login(final LoginCredentials credentials) {
        authentification
            .login(credentials)
            .doOnSubscribe(d -> ZorroLogger.log("Login to Dukascopy started..."))
            .blockingAwait(2000L, TimeUnit.MILLISECONDS);

        if (client.isConnected()) {
            ZorroLogger.log("Client is now connected...");
        } else {
            ZorroLogger.log("Client is NOT connected!");
        }

        return client.isConnected()
                ? ReturnCodes.LOGIN_OK
                : ReturnCodes.LOGIN_FAIL;
    }

    public int doLogout() {
        authentification
            .logout()
            .blockingAwait();
        ZorroLogger.log("Logged out from Dukascopy.");
        return ReturnCodes.LOGOUT_OK;
    }
}
