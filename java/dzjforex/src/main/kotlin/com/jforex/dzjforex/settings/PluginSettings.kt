package com.jforex.dzjforex.settings

import org.aeonbits.owner.Config

@Config.Sources(
    "file:./Plugin/dukascopy/Plugin.properties",
    "classpath:Plugin.properties"
)
interface PluginSettings : Config
{
    @Config.Key("platform.cachedirectory")
    @Config.DefaultValue("./Plugin/dukascopy/.cache")
    fun cacheDirectory(): String

    @Config.Key("platform.login.retrydelay")
    @Config.DefaultValue("5000")
    fun loginRetryDelay(): Long

    @Config.Key("order.labelprefix")
    @Config.DefaultValue("zorro")
    fun labelPrefix(): String

    @Config.Key("history.tickfetchmillis")
    @Config.DefaultValue("1800000")
    fun tickfetchmillis(): Long

    @Config.Key("plugin.waitfortradeableinstrument")
    @Config.DefaultValue("true")
    fun waitForTradeableInstrument(): Boolean

    @Config.Key("plugin.maxticks")
    @Config.DefaultValue("1500")
    fun maxTicks(): Int

    @Config.Key("plugin.progressinterval")
    @Config.DefaultValue("250")
    fun zorroProgressInterval(): Long

    @Config.Key("plugin.usetickcallback")
    @Config.DefaultValue("true")
    fun useTickCallback(): Boolean

    @Config.Key("dukascopy.usepin")
    @Config.DefaultValue("false")
    fun useLoginPin(): Boolean
}

