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

    @Config.Key("order.lotsize")
    @Config.DefaultValue("1000")
    fun lotSize(): Double

    @Config.Key("order.lotscale")
    @Config.DefaultValue("1000000")
    fun lotScale(): Double
}