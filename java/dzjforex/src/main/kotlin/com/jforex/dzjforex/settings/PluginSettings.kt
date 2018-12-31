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

    @Config.Key("zorro.progressinterval")
    @Config.DefaultValue("250")
    fun zorroProgressInterval(): Long

    @Config.Key("order.labelprefix")
    @Config.DefaultValue("zorro")
    fun labelPrefix(): String

    @Config.Key("order.trade.maxwait")
    @Config.DefaultValue("30")
    fun maxSecondsForOrderBuy(): Long

    @Config.Key("history.access.retries")
    @Config.DefaultValue("10")
    fun historyAccessRetries(): Long

    @Config.Key("history.access.retrydelay")
    @Config.DefaultValue("1000")
    fun historyAccessRetryDelay(): Long

    @Config.Key("plugin.waitfortradeableinstrument")
    @Config.DefaultValue("true")
    fun waitForTradeableInstrument(): Boolean

    @Config.Key("plugin.maxticks")
    @Config.DefaultValue("1500")
    fun maxTicks(): Int
}

