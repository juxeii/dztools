package com.jforex.dzjforex.asset

import arrow.core.getOrElse
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.QuoteProvider
import com.jforex.dzjforex.misc.instrumentFromAssetName
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import org.apache.logging.log4j.LogManager

class BrokerAsset(private val quoteProvider: QuoteProvider)
{
    private val logger = LogManager.getLogger(BrokerAsset::class.java)

    fun get(
        assetName: String,
        out_AssetParamsToFill: DoubleArray
    ): Int
    {
        return instrumentFromAssetName(assetName)
            .map {
                fillParams(it, out_AssetParamsToFill)
                it
            }
            .fold({ ASSET_UNAVAILABLE }) { ASSET_AVAILABLE }
    }


    private fun fillParams(
        instrument: Instrument,
        out_AssetParamsToFill: DoubleArray
    )
    {
        logger.debug("Fillin asset params")
    }
}