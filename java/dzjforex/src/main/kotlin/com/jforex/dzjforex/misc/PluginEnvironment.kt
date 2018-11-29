package com.jforex.dzjforex.misc

import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.settings.PluginSettings

data class PluginEnvironment(
    val client: IClient,
    val pluginStrategy: PluginStrategy,
    val pluginSettings: PluginSettings
)