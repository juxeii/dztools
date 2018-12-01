package com.jforex.dzjforex.asset

import arrow.data.ReaderApi
import arrow.data.map
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.instrumentFromAssetName
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getAssetData(
    assetName: String,
    out_AssetParamsToFill: DoubleArray
) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        instrumentFromAssetName(assetName)
            .map { fillAssetParams(it, out_AssetParamsToFill).run(env) }
            .fold({ ASSET_UNAVAILABLE }) { ASSET_AVAILABLE }
    }

private fun fillAssetParams(
    instrument: Instrument,
    out_AssetParamsToFill: DoubleArray
) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        val quoteProvider = env.pluginStrategy.quoteProvider
        val price = quoteProvider.ask(instrument)
        val spread = quoteProvider.spread(instrument)

        out_AssetParamsToFill[0] = price
        out_AssetParamsToFill[1] = spread
    }