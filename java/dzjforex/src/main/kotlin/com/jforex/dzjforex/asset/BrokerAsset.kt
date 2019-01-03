package com.jforex.dzjforex.asset

import com.dukascopy.api.Instrument
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.account.AccountApi.pipCost
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InstrumentApi.createInstrument
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.kforexutils.instrument.spread
import com.jforex.kforexutils.instrument.tick
import com.jforex.kforexutils.misc.asCost

object BrokerAssetApi
{
    fun <F> ContextDependencies<F>.brokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray) =
        createInstrument(assetName)
            .flatMap { instrument -> fillAssetData(instrument, out_AssetParamsToFill) }
            .map { ASSET_AVAILABLE }
            .handleError { error ->
                logger.error(
                    "BrokerAsset failed! Error message: ${error.message} " +
                            "Stack trace: ${getStackTrace(error)}"
                )
                ASSET_UNAVAILABLE
            }

    fun <F> ContextDependencies<F>.fillAssetData(instrument: Instrument, out_AssetParamsToFill: DoubleArray) =
        bindingCatch {
            val iPrice = 0
            val iSpread = 1
            val iVolume = 2
            val iPip = 3
            val iPipCost = 4
            val iLotAmount = 5
            val iMarginCost = 6
            val tick = instrument.tick()

            out_AssetParamsToFill[iPrice] = tick.ask
            out_AssetParamsToFill[iSpread] = instrument.spread()
            out_AssetParamsToFill[iVolume] = tick.askVolume
            out_AssetParamsToFill[iPip] = instrument.pipValue
            out_AssetParamsToFill[iPipCost] = pipCost(instrument).bind()
            out_AssetParamsToFill[iLotAmount] = instrument.minTradeAmount
            out_AssetParamsToFill[iMarginCost] = getMarginCost(instrument).bind()

            logger.debug(
                "$instrument AssetData: price ${out_AssetParamsToFill[iPrice]} " +
                        "spread ${out_AssetParamsToFill[iSpread]} " +
                        "volume ${out_AssetParamsToFill[iVolume]} " +
                        "pipValue ${out_AssetParamsToFill[iPip]} " +
                        "pipCost ${out_AssetParamsToFill[iPipCost]} " +
                        "lotAmount ${out_AssetParamsToFill[iLotAmount]} " +
                        "marginCost ${out_AssetParamsToFill[iMarginCost]}"
            )
        }

    fun <F> ContextDependencies<F>.getMarginCost(instrument: Instrument) = bindingCatch {
        val rateToAccountCurrency = getRateToAccountCurrency(instrument).bind()
        (rateToAccountCurrency * (instrument.minTradeAmount / account.leverage)).asCost()
    }

    fun <F> ContextDependencies<F>.getRateToAccountCurrency(instrument: Instrument) = bindingCatch {
        if (instrument.primaryJFCurrency == account.accountCurrency) 1.0
        else jfContext
            .utils
            .getRate(instrument.primaryJFCurrency, account.accountCurrency, OfferSide.ASK)
    }
}
