package com.jforex.dzjforex.brokerlogin;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.connection.LoginCredentials;

public class CredentialsFactory {

    private final PinProvider pinProvider;
    private final PluginConfig pluginConfig;
    private final String demoTypeName;

    public CredentialsFactory(final PinProvider pinProvider,
                              final PluginConfig pluginConfig) {
        this.pinProvider = pinProvider;
        this.pluginConfig = pluginConfig;

        demoTypeName = pluginConfig.demoLoginType();
    }

    public LoginCredentials create(final BrokerLoginData brokerLoginData) {
        final String loginType = brokerLoginData.loginType();

        return new LoginCredentials(jnlpAddressForType(loginType),
                                    brokerLoginData.username(),
                                    brokerLoginData.password(),
                                    pinForType(loginType));
    }

    private String jnlpAddressForType(final String loginType) {
        return loginType.equals(demoTypeName)
                ? pluginConfig.demoConnectURL()
                : pluginConfig.realConnectURL();
    }

    private String pinForType(final String loginType) {
        return loginType.equals(demoTypeName)
                ? null
                : pinProvider.getPin();
    }
}
