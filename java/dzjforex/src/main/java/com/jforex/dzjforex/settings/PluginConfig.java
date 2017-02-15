package com.jforex.dzjforex.settings;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "classpath:PluginConfig.properties" })
public interface PluginConfig extends Config {
    @DefaultValue(".\\Plugin\\dukascopy\\.cache")
    String CACHE_DIR();

    @DefaultValue("zorro")
    String ORDER_PREFIX_LABEL();

    @DefaultValue("3f")
    double DEFAULT_SLIPPAGE();

    @DefaultValue("1000")
    long CONNECTION_WAIT_TIME();

    @DefaultValue("10")
    int CONNECTION_RETRIES();

    @DefaultValue("https://www.dukascopy.com/client/demo/jclient/jforex.jnlp")
    String CONNECT_URL_DEMO();

    @DefaultValue("https://www.dukascopy.com/client/live/jclient/jforex.jnlp")
    String CONNECT_URL_REAL();

    @DefaultValue("time-a.nist.gov")
    String NTP_TIME_SERVER_URL();

    @DefaultValue("3000")
    int NTP_TIMEOUT();

    @DefaultValue("300000")
    int SERVERTIME_SYNC_MILLIS();

    @DefaultValue("200")
    long SUBSCRIPTION_WAIT_TIME();

    @DefaultValue("10")
    int SUBSCRIPTION_WAIT_TIME_RETRIES();

    @DefaultValue("1000000")
    int LOT_SCALE();

    @DefaultValue("3000")
    long ORDER_UPDATE_WAITTIME();
}
