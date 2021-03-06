package com.jforex.dzjforex.asset

import com.dukascopy.api.Instrument
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.account.AccountApi.pipCost
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InvalidAssetNameException
import com.jforex.dzjforex.misc.PluginApi.createInstrument
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.kforexutils.instrument.spread
import com.jforex.kforexutils.instrument.tick
import com.jforex.kforexutils.misc.asCost

object BrokerAssetApi
{
    fun <F> ContextDependencies<F>.brokerAsset(assetName: String) =
        createInstrument(assetName)
            .flatMap { instrument -> getAssetData(instrument) }
            .handleErrorWith { error -> processError(error) }

    fun <F> ContextDependencies<F>.processError(error: Throwable) = delay {
        when (error)
        {
            is InvalidAssetNameException ->
                natives.logAndPrintErrorOnZorro("BrokerAsset: Asset name ${error.assetName} is invalid!")
            else -> logger.error(
                "BrokerAsset failed! Error message: ${error.message} " +
                        "Stack trace: ${getStackTrace(error)}"
            )
        }
        BrokerAssetData(ASSET_UNAVAILABLE)
    }

    fun <F> ContextDependencies<F>.getAssetData(instrument: Instrument) = bindingCatch {
        with(instrument.tick()){
            BrokerAssetData(
                returnCode = ASSET_AVAILABLE,
                price = ask,
                spread = instrument.spread(),
                volume = askVolume,
                pip = instrument.pipValue,
                pipCost = pipCost(instrument).bind(),
                lotAmount = instrument.minTradeAmount,
                marginCost = getMarginCost(instrument).bind()
            )
        }
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
