package com.jforex.dzjforex.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "classpath:Plugin.properties" })
public interface PluginConfig extends Config {
    @Key("platform.cachedirectory")
    @DefaultValue(".cache")
    String CACHE_DIR();

    @Key("platform.subscribewaittime")
    @DefaultValue("200")
    long SUBSCRIPTION_WAIT_TIME();

    @Key("platform.subscribemaxretries")
    @DefaultValue("10")
    int SUBSCRIPTION_WAIT_TIME_RETRIES();

    @Key("connection.waittime")
    @DefaultValue("200")
    long CONNECTION_WAIT_TIME();

    @Key("connection.maxretries")
    @DefaultValue("10")
    int CONNECTION_RETRIES();

    @Key("connection.demourl")
    @DefaultValue("http://platform.dukascopy.com/demo_3/jforex_3.jnlp")
    String CONNECT_URL_DEMO();

    @Key("connection.liveurl")
    @DefaultValue("http://platform.dukascopy.com/live_3/jforex_3.jnlp")
    String CONNECT_URL_REAL();

    @Key("ntp.serverurl")
    @DefaultValue("time.nist.gov")
    String NTP_TIME_SERVER_URL();

    @Key("ntp.timeout")
    @DefaultValue("3000")
    int NTP_TIMEOUT();

    @Key("ntp.syntime")
    @DefaultValue("300000")
    int SERVERTIME_SYNC_MILLIS();

    @Key("order.labelprefix")
    @DefaultValue("zorro")
    String ORDER_PREFIX_LABEL();

    @Key("order.updatewaittime")
    @DefaultValue("3000")
    long ORDER_UPDATE_WAITTIME();

    @Key("order.maxslippage")
    @DefaultValue("3.0")
    double DEFAULT_SLIPPAGE();

    @Key("order.lotscale")
    @DefaultValue("1000000")
    int LOT_SCALE();

    @Key("order.lotsize")
    @DefaultValue("1000")
    double LOT_SIZE();
}
