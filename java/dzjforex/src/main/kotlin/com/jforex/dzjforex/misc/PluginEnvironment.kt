package com.jforex.dzjforex.misc

import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives

data class PluginEnvironment(
    val client: IClient,
    val pluginStrategy: PluginStrategy,
    val pluginSettings: PluginSettings,
    val natives: ZorroNatives,
    var quotes: Quotes = emptyMap()
)