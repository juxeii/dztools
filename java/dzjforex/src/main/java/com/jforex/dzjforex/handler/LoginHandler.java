package com.jforex.dzjforex.handler;

import java.util.concurrent.TimeUnit;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.misc.PinProvider;
import com.jforex.dzjforex.settings.PluginConfig;
import com.jforex.dzjforex.settings.ReturnCodes;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

public class LoginHandler {

    private final IClient client;
    private final Authentification authentification;
    private final PinProvider pinProvider;
    private final PluginConfig pluginConfig;

    public LoginHandler(final IClient client,
                        final Authentification authentification,
                        final PinProvider pinProvider,
                        final PluginConfig pluginConfig) {
        this.client = client;
        this.authentification = authentification;
        this.pinProvider = pinProvider;
        this.pluginConfig = pluginConfig;
    }

    public int doLogin(final String userName,
                       final String password,
                       final String type) {
        final String pin = type.equals("Demo")
                ? null
                : pinProvider.getPin();
        final String jnlpAdress = type.equals("Demo")
                ? pluginConfig.CONNECT_URL_DEMO()
                : pluginConfig.CONNECT_URL_REAL();
        final LoginCredentials credentials = new LoginCredentials(jnlpAdress,
                                                                  userName,
                                                                  password,
                                                                  pin);
        return login(credentials);
    }

    private int login(final LoginCredentials credentials) {
        authentification
            .login(credentials)
            .doOnSubscribe(d -> ZorroLogger.log("Log in started..."))
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
