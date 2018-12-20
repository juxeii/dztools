package com.jforex.dzjforex.asset

import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.QuoteProviderDependencies
import com.jforex.dzjforex.misc.QuotesApi.getAsk
import com.jforex.dzjforex.misc.QuotesApi.getSpread
import com.jforex.dzjforex.misc.createQuoteProviderApi
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

data class AssetParams(val price: Double, val spread: Double)

fun createBrokerAssetApi(instrument: Instrument): BrokerAssetDependencies =
    BrokerAssetDependencies(instrument, createQuoteProviderApi())

interface BrokerAssetDependencies : QuoteProviderDependencies
{
    val instrument: Instrument

    companion object
    {
        operator fun invoke(
            instrument: Instrument,
            quoteProviderDependencies: QuoteProviderDependencies
        ): BrokerAssetDependencies =
            object : BrokerAssetDependencies, QuoteProviderDependencies by quoteProviderDependencies
            {
                override val instrument = instrument
            }
    }
}

object BrokerAssetApi
{
    fun BrokerAssetDependencies.getAssetParams(): AssetParams
    {
        val ask = getAsk(instrument)
        val spread = getSpread(instrument)

        return AssetParams(price = ask, spread = spread)
    }
}
