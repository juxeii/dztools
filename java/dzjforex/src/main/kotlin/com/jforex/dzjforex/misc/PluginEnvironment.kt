package com.jforex.dzjforex.misc

import com.dukascopy.api.system.IClient

data class PluginEnvironment(
    val client: IClient,
    val pluginStrategy: PluginStrategy
)