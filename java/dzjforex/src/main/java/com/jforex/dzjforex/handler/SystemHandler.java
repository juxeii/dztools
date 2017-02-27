package com.jforex.dzjforex.handler;

import java.time.Clock;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.ClientProvider;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.programming.client.ClientUtil;

public class SystemHandler {

    private final IClient client;
    private final ClientUtil clientUtil;
    private final Zorro zorro;
    private final InfoStrategy infoStrategy;
    private final PluginConfig pluginConfig;
    private final Clock clock;

    public SystemHandler(final PluginConfig pluginConfig,
                         final Clock clock) {
        this.pluginConfig = pluginConfig;
        this.clock = clock;

        client = ClientProvider.get();
        clientUtil = new ClientUtil(client, pluginConfig.cacheDirectory());
        zorro = new Zorro(pluginConfig);
        infoStrategy = new InfoStrategy();
    }

    public IClient client() {
        return client;
    }

    public ClientUtil clientUtil() {
        return clientUtil;
    }

    public Zorro zorro() {
        return zorro;
    }

    public InfoStrategy infoStrategy() {
        return infoStrategy;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public Clock clock() {
        return clock;
    }

    public long startStrategy() {
        client.setSubscribedInstruments(Sets.newHashSet(Instrument.EURUSD));
        return client.startStrategy(infoStrategy);
    }
}
