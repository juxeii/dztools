package com.jforex.dzjforex.connection;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.connection.LoginCredentials;

public class CredentialsFactory {

    private final PinProvider pinProvider;
    private final PluginConfig pluginConfig;

    private static final String demoName = "Demo";

    public CredentialsFactory(final PinProvider pinProvider,
                              final PluginConfig pluginConfig) {
        this.pinProvider = pinProvider;
        this.pluginConfig = pluginConfig;
    }

    public LoginCredentials create(final String username,
                                   final String password,
                                   final String loginType) {
        return new LoginCredentials(createJnlpAdress(loginType),
                                    username,
                                    password,
                                    createPin(loginType));
    }

    private String createJnlpAdress(final String loginType) {
        return loginType.equals(demoName)
                ? pluginConfig.CONNECT_URL_DEMO()
                : pluginConfig.CONNECT_URL_REAL();
    }

    private String createPin(final String loginType) {
        return loginType.equals(demoName)
                ? null
                : pinProvider.getPin();
    }
}
