package com.jforex.dzjforex.asset

import arrow.Kind
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountApi.lotSize
import com.jforex.dzjforex.account.AccountApi.pipCost
import com.jforex.dzjforex.account.accountApi
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.QuotesApi.getAsk
import com.jforex.dzjforex.misc.QuotesApi.getSpread
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE

fun createBrokerAssetApi() = BrokerAssetDependencies(contextApi, createQuoteProviderApi())

interface BrokerAssetDependencies<F> : ContextDependencies<F>, QuoteProviderDependencies
{
    companion object
    {
        operator fun <F> invoke(
            ContextDependencies: ContextDependencies<F>,
            quoteProviderDependencies: QuoteProviderDependencies
        ): BrokerAssetDependencies<F> =
            object : BrokerAssetDependencies<F>,
                ContextDependencies<F> by ContextDependencies,
                QuoteProviderDependencies by quoteProviderDependencies
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
        val unsupportedValue = 0.0
        out_AssetParamsToFill[0] = getAsk(instrument)
        out_AssetParamsToFill[1] = getSpread(instrument)

        out_AssetParamsToFill[2] = unsupportedValue;
        out_AssetParamsToFill[3] = instrument.pipValue;
        out_AssetParamsToFill[4] = pipCost(instrument)
        out_AssetParamsToFill[5] = lotSize()
        out_AssetParamsToFill[7] = unsupportedValue;
        out_AssetParamsToFill[8] = unsupportedValue;

        /*logger.debug(
            "Asset Params: ask ${getAsk(instrument)}, " +
                    "spread ${getSpread(instrument)}," +
                    " pipValue ${instrument.pipValue}," +
                    " pipCost ${pipCost(instrument)}, " +
                    "lotSize ${lotSize()}"
        )*/
    }
}
