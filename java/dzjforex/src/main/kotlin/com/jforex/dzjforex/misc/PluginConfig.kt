package com.jforex.dzjforex.misc

import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives

data class PluginConfig(
    val client: IClient,
    val pluginSettings: PluginSettings,
    val natives: ZorroNatives
)