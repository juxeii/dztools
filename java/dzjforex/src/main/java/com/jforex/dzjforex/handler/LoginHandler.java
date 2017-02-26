package com.jforex.dzjforex.handler;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerLogin;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.misc.PinProvider;
import com.jforex.programming.client.ClientUtil;

public class LoginHandler {

    private final BrokerLogin brokerLogin;

    public LoginHandler(final SystemHandler systemHandler) {
        final ClientUtil clientUtil = systemHandler.clientUtil();
        final IClient client = clientUtil.client();
        final PluginConfig pluginConfig = systemHandler.pluginConfig();

        final PinProvider pinProvider = new PinProvider(client, pluginConfig.realConnectURL());
        final CredentialsFactory credentialsFactory = new CredentialsFactory(pinProvider, pluginConfig);
        final LoginExecutor loginExecutor = new LoginExecutor(clientUtil.authentification(),
                                                              credentialsFactory,
                                                              systemHandler.zorro());

        brokerLogin = new BrokerLogin(loginExecutor,
                                      client,
                                      pluginConfig);
    }

    public int brokerLogin(final String userName,
                           final String password,
                           final String type) {
        return brokerLogin.login(userName,
                                 password,
                                 type);
    }
}
