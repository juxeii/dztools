package com.jforex.dzjforex;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.ClientProvider;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.programming.client.ClientUtil;

public class SystemComponents {

    private final IClient client;
    private final ClientUtil clientUtil;
    private final InfoStrategy infoStrategy;
    private final PluginConfig pluginConfig;

    public SystemComponents(final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;

        client = ClientProvider.get();
        clientUtil = new ClientUtil(client, pluginConfig.cacheDirectory());
        infoStrategy = new InfoStrategy();
    }

    public IClient client() {
        return client;
    }

    public ClientUtil clientUtil() {
        return clientUtil;
    }

    public InfoStrategy infoStrategy() {
        return infoStrategy;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }
}
