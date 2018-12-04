package com.jforex.dzjforex.misc

import com.jforex.dzjforex.zorro.Quotes
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

data class PluginConfigExt(
    val pluginConfig: PluginConfig,
    val infoStrategy: KForexUtilsStrategy,
    var kForexUtils: KForexUtils,
    val quotes: Quotes
)