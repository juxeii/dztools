package com.jforex.dzjforex.asset

import arrow.data.ReaderApi
import arrow.data.fix
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.PluginConfig
import com.jforex.dzjforex.misc.getAsk
import com.jforex.dzjforex.misc.getSpread
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getAssetParams(instrument: Instrument) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val price = getAsk(instrument).bind()
        val spread = getSpread(instrument).bind()

        AssetParams(
            price = price,
            spread = spread
        )
    }.fix()