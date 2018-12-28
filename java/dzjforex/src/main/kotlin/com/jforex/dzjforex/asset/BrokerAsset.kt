package com.jforex.dzjforex.asset

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountApi.pipCost
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.QuotesProviderApi.getAsk
import com.jforex.dzjforex.misc.QuotesProviderApi.getSpread
import com.jforex.dzjforex.misc.QuotesProviderApi.getTick
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE

fun createBrokerAssetApi() = QuoteDependencies(contextApi, createQuoteProviderApi())

object BrokerAssetApi
{
    fun <F> QuoteDependencies<F>.brokerAsset(
        assetName: String,
        out_AssetParamsToFill: DoubleArray
    ): Kind<F, Int> = bindingCatch {
        val instrument = fromAssetName(assetName).bind()
        fillParams(out_AssetParamsToFill, instrument).bind()

        ASSET_AVAILABLE
    }.handleError { ASSET_UNAVAILABLE }

    private fun <F> QuoteDependencies<F>.fillParams(
        out_AssetParamsToFill: DoubleArray,
        instrument: Instrument
    ): Kind<F, Unit> = catch {
        val unsupportedValue = 0.0
        val tick = getTick(instrument)
        out_AssetParamsToFill[0] = tick.ask
        out_AssetParamsToFill[1] = getSpread(instrument)

        out_AssetParamsToFill[2] = tick.askVolume
        out_AssetParamsToFill[3] = instrument.pipValue;
        out_AssetParamsToFill[4] = pipCost(instrument)
        out_AssetParamsToFill[5] = instrument.minTradeAmount
        out_AssetParamsToFill[7] = unsupportedValue;
        out_AssetParamsToFill[8] = unsupportedValue;

        logger.debug(
            "Asset Params: ask ${getAsk(instrument)}, " +
                    " spread ${getSpread(instrument)}," +
                    " volume ${tick.askVolume}," +
                    " pipValue ${instrument.pipValue}," +
                    " pipCost ${pipCost(instrument)}, " +
                    " lotSize ${instrument.minTradeAmount}"
        )    }
}
