package com.jforex.dzjforex.connection;

import java.util.concurrent.TimeUnit;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

import io.reactivex.Observable;

public class LoginHandler {

    private final IClient client;
    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;
    private final PluginConfig pluginConfig;

    public LoginHandler(final IClient client,
                        final Authentification authentification,
                        final CredentialsFactory credentialsFactory,
                        final PluginConfig pluginConfig) {
        this.client = client;
        this.authentification = authentification;
        this.credentialsFactory = credentialsFactory;
        this.pluginConfig = pluginConfig;
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
        return authentification
            .login(credentials)
            .andThen(Observable
                .interval(pluginConfig.CONNECTION_WAIT_TIME(), TimeUnit.MILLISECONDS)
                .take(pluginConfig.CONNECTION_RETRIES())
                .takeUntil(att -> (Boolean) client.isConnected())
                .map(waitAttempt -> client.isConnected()
                        ? ReturnCodes.LOGIN_OK
                        : ReturnCodes.LOGIN_FAIL))
            .onErrorResumeNext(err -> {
                ZorroLogger.showError("Failed to login with exception " + err.getMessage());
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
