package com.jforex.dzjforex.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:./Plugin/dukascopy/Plugin.properties",
        "classpath:Plugin.properties" })
public interface PluginConfig extends Config {
    @Key("platform.cachedirectory")
    @DefaultValue("./Plugin/dukascopy/.cache")
    String cacheDirectory();

    @Key("platform.login.retrydelay")
    @DefaultValue("5000")
    long loginRetryDelay();

    @Key("order.labelprefix")
    @DefaultValue("zorro")
    String orderLabelPrefix();

    @Key("order.transmit.retries")
    @DefaultValue("3")
    int orderSubmitRetries();

    @Key("order.transmit.retrydelay")
    @DefaultValue("1500")
    long orderSubmitRetryDelay();

    @Key("order.maxslippage")
    @DefaultValue("3.0")
    double orderMaxSlippage();

    @Key("order.minpipsforstoploss")
    @DefaultValue("5.0")
    double minPipsForSL();

    @Key("order.lotscale")
    @DefaultValue("1000000")
    double lotScale();

    @Key("order.lotsize")
    @DefaultValue("1000")
    double lotSize();

    @Key("order.automerge")
    @DefaultValue("true")
    boolean isAutoMerge();

    @Key("history.access.retries")
    @DefaultValue("3")
    int historyAccessRetries();

    @Key("history.access.retrydelay")
    @DefaultValue("1000")
    long historyAccessRetryDelay();

    @Key("history.tickfetchmillis")
    @DefaultValue("900000")
    long tickFetchMillis();

    @Key("history.orderdays")
    @DefaultValue("7")
    int historyOrderInDays();

    @Key("account.demologintype")
    @DefaultValue("Demo")
    String demoLoginType();

    @Key("account.reallogintype")
    @DefaultValue("Real")
    String realLoginType();

    @Key("connection.demourl")
    @DefaultValue("http://platform.dukascopy.com/demo_3/jforex_3.jnlp")
    String demoConnectURL();

    @Key("connection.liveurl")
    @DefaultValue("http://platform.dukascopy.com/live_3/jforex_3.jnlp")
    String realConnectURL();

    @Key("ntp.serverurl")
    @DefaultValue("time.nist.gov")
    String ntpServerURL();

    @Key("ntp.syncinterval")
    @DefaultValue("300000")
    long ntpSynchInterval();

    @Key("ntp.timeout")
    @DefaultValue("2000")
    long ntpTimeOut();

    @Key("ntp.retrydelay")
    @DefaultValue("5000")
    long ntpRetryDelay();

    @Key("zorro.progressinterval")
    @DefaultValue("250")
    long zorroProgressInterval();
}
