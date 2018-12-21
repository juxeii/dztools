package com.jforex.dzjforex.asset

import arrow.Kind
import arrow.core.fix
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.QuoteProviderDependencies
import com.jforex.dzjforex.misc.QuotesApi.getAsk
import com.jforex.dzjforex.misc.QuotesApi.getSpread
import com.jforex.dzjforex.misc.createQuoteProviderApi
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.InstrumentFunc

fun createBrokerAssetApi() = BrokerAssetDependencies(createQuoteProviderApi(), IO.monadError())

interface BrokerAssetDependencies<F> : QuoteProviderDependencies, InstrumentFunc<F>
{
    companion object
    {
        operator fun <F> invoke(
            quoteProviderDependencies: QuoteProviderDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerAssetDependencies<F> =
            object : BrokerAssetDependencies<F>,
                QuoteProviderDependencies by quoteProviderDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerAssetApi
{
    fun <F> BrokerAssetDependencies<F>.create(
        assetName: String,
        out_AssetParamsToFill: DoubleArray
    ): Kind<F, Int> = bindingCatch {
        val instrument = fromAssetName(assetName).bind()
        fillParams(out_AssetParamsToFill, instrument).bind()

        ASSET_AVAILABLE
    }.handleError { ASSET_UNAVAILABLE }

    private fun <F> BrokerAssetDependencies<F>.fillParams(
        out_AssetParamsToFill: DoubleArray,
        instrument: Instrument
    ): Kind<F, Unit> = catch {
        val ask = getAsk(instrument)
        val spread = getSpread(instrument)

        out_AssetParamsToFill[0] = ask
        out_AssetParamsToFill[1] = spread
    }
}
