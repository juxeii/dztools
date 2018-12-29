package com.jforex.dzjforex.asset

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.account.AccountApi.pipCost
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.QuotesProviderApi.getAsk
import com.jforex.dzjforex.misc.QuotesProviderApi.getSpread
import com.jforex.dzjforex.misc.QuotesProviderApi.getTick
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.kforexutils.instrument.InstrumentFactory

data class BrokerAssetData(
    val price: Double,
    val spread: Double,
    val volume: Double,
    val pip: Double,
    val pipCost: Double,
    val lotAmount: Double,
    val marginCost: Double
)

sealed class BrokerAssetResult(val returnCode: Int)
{
    data class Failure(val code: Int) : BrokerAssetResult(code)
    data class Success(val code: Int, val data: BrokerAssetData) : BrokerAssetResult(code)
}
typealias BrokerAssetFailure = BrokerAssetResult.Failure
typealias BrokerAssetSuccess = BrokerAssetResult.Success

fun createBrokerAssetApi() = QuoteDependencies(contextApi, createQuoteProviderApi())

object BrokerAssetApi
{
    fun <F> QuoteDependencies<F>.brokerAsset(assetName: String): Kind<F, BrokerAssetResult> =
        fromAssetName(assetName)
            .flatMap { instrument -> createAssetData(instrument) }
            .map { assetData -> BrokerAssetSuccess(ASSET_AVAILABLE, assetData) }
            .handleError { error ->
                logger.debug("BrokerAsset failed! Error: ${error} Stack trace: ${getStackTrace(error)}")
                BrokerAssetFailure(ASSET_UNAVAILABLE)
            }

    private fun <F> QuoteDependencies<F>.createAssetData(instrument: Instrument): Kind<F, BrokerAssetData> =
        bindingCatch {
            val tick = getTick(instrument)
            val assetData = BrokerAssetData(
                price = tick.ask,
                spread = getSpread(instrument),
                volume = tick.askVolume,
                pip = instrument.pipValue,
                pipCost = pipCost(instrument),
                lotAmount = instrument.minTradeAmount,
                marginCost = getMarginCost(instrument).bind()
            )
            logger.debug("$instrument AssetData: $assetData")
            assetData
        }

    fun <F> QuoteDependencies<F>.getRateToAccountCurrency(instrument: Instrument): Kind<F, Double> = invoke {
        if (instrument.primaryJFCurrency == account.accountCurrency) 1.0
        else context
            .utils
            .getRate(instrument.primaryJFCurrency, account.accountCurrency, OfferSide.ASK)
    }

    fun <F> QuoteDependencies<F>.getMarginCost(instrument: Instrument): Kind<F, Double> = bindingCatch {
        val rateToAccountCurrency = getRateToAccountCurrency(instrument).bind()
        rateToAccountCurrency * (instrument.minTradeAmount / account.leverage)
    }
}
