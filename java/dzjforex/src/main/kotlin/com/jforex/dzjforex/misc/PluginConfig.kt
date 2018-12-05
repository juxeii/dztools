package com.jforex.dzjforex.misc

import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.Quotes
import com.jforex.dzjforex.zorro.ZorroNatives
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

internal data class PluginConfig(
    val client: IClient,
    val pluginSettings: PluginSettings,
    val natives: ZorroNatives,
    val infoStrategy: KForexUtilsStrategy,
    var kForexUtils: KForexUtils,
    val quotes: Quotes
)