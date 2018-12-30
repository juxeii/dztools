package com.jforex.dzjforex.buy

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.command.getBcSlippage
import com.jforex.dzjforex.command.maybeBcOrderText
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.PluginApi.contractsToAmount
import com.jforex.dzjforex.misc.QuotesProviderApi.getAsk
import com.jforex.dzjforex.misc.QuotesProviderApi.getBid
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import com.jforex.dzjforex.zorro.BROKER_BUY_OPPOSITE_CLOSE
import com.jforex.dzjforex.zorro.BROKER_BUY_TIMEOUT
import com.jforex.kforexutils.engine.submit
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.price.Pips
import com.jforex.kforexutils.price.Price
import com.jforex.kforexutils.settings.TradingSettings
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

sealed class BrokerBuyResult(val returnCode: Int)
{
    data class Failure(val code: Int) : BrokerBuyResult(code)
    data class Success(val code: Int, val price: Double) : BrokerBuyResult(code)
}
typealias BrokerBuyFailure = BrokerBuyResult.Failure
typealias BrokerBuySuccess = BrokerBuyResult.Success

fun createBrokerBuyApi() = QuoteDependencies(contextApi, createQuoteProviderApi())

object BrokerBuyApi
{
    private data class BuyParameter(
        val label: String,
        val instrument: Instrument,
        val orderCommand: IEngine.OrderCommand,
        val amount: Double,
        val slPrice: Double,
        val price: Double,
        val slippage: Double,
        val comment: String
    )

    fun <F> QuoteDependencies<F>.brokerBuy(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double
    ): Kind<F, BrokerBuyResult> =
        createBuyParameter(
            assetName = assetName,
            contracts = contracts,
            slDistance = slDistance,
            limitPrice = limitPrice
        )
            .flatMap { submitOrder(it) }
            .flatMap { processOrderAndGetResult(it, slDistance, limitPrice != 0.0) }
            .handleError { error ->
                logger.error("BrokerBuy failed! Error: $error Stack trace: ${getStackTrace(error)}")
                val errorCode = if (error is TimeoutException) BROKER_BUY_TIMEOUT else BROKER_BUY_FAIL
                BrokerBuyFailure(errorCode)
            }

    private fun <F> QuoteDependencies<F>.createBuyParameter(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double
    ): Kind<F, BuyParameter> = bindingCatch {
        val instrument = fromAssetName(assetName).bind()
        val label = createLabel().bind()
        val isLimitOrder = limitPrice != 0.0
        val orderCommand = createOrderCommand(contracts, isLimitOrder)
        val amount = contractsToAmount(contracts)
        val roundedLimitPrice = createLimitPrice(instrument, limitPrice)
        val slPrice = createSLPrice(slDistance, instrument, orderCommand, limitPrice)
        val slippage = getBcSlippage()
        val comment = maybeBcOrderText().fold({ "" }) { it }

        BuyParameter(
            label = label,
            instrument = instrument,
            orderCommand = orderCommand,
            amount = amount,
            slPrice = slPrice,
            price = roundedLimitPrice,
            slippage = slippage,
            comment = comment
        )
    }

    private fun <F> QuoteDependencies<F>.submitOrder(buyParameter: BuyParameter): Kind<F, IOrder> =
        catch {
            engine
                .submit(
                    label = buyParameter.label,
                    instrument = buyParameter.instrument,
                    orderCommand = buyParameter.orderCommand,
                    amount = buyParameter.amount,
                    stopLossPrice = buyParameter.slPrice,
                    price = buyParameter.price,
                    slippage = buyParameter.slippage,
                    comment = buyParameter.comment
                )
                .filter {
                    if (buyParameter.price != 0.0) it.type == OrderEventType.SUBMIT_OK
                    else it.type == OrderEventType.FULLY_FILLED
                }
                .timeout(pluginSettings.maxSecondsForOrderFill(), TimeUnit.SECONDS)
                .map { it.order }
                .blockingFirst()
        }

    fun <F> QuoteDependencies<F>.createLabel(): Kind<F, String> =
        just(pluginSettings.labelPrefix() + System.currentTimeMillis().toString())

    fun createOrderCommand(contracts: Int, isLimitOrder: Boolean): IEngine.OrderCommand =
        when
        {
            contracts >= 0 && !isLimitOrder -> IEngine.OrderCommand.BUY
            contracts >= 0 && isLimitOrder -> IEngine.OrderCommand.BUYLIMIT
            contracts < 0 && !isLimitOrder -> IEngine.OrderCommand.SELL
            else -> IEngine.OrderCommand.SELLLIMIT
        }

    fun createLimitPrice(instrument: Instrument, limitPrice: Double) =
        if (limitPrice != 0.0) Price(instrument, limitPrice).toDouble() else 0.0

    fun <F> QuoteDependencies<F>.createSLPrice(
        slDistance: Double,
        instrument: Instrument,
        orderCommand: IEngine.OrderCommand,
        limitPrice: Double
    ): Double
    {
        if (slDistance <= 0) return TradingSettings.noSLPrice
        val openPrice = if (limitPrice != 0.0) Price(instrument, limitPrice)
        else Price(
            instrument,
            if (orderCommand == IEngine.OrderCommand.BUY) getBid(instrument) else getAsk(instrument)
        )
        val pipDistance = Pips(slDistance)
        val slPrice = if (orderCommand == IEngine.OrderCommand.BUY) openPrice - pipDistance
        else openPrice + pipDistance
        return slPrice.toDouble()
    }

    fun <F> QuoteDependencies<F>.processOrderAndGetResult(
        order: IOrder,
        slDistance: Double,
        isLimitOrder: Boolean
    ): Kind<F, BrokerBuyResult> =
        catch {
            val returnCode = if (order.state == if (isLimitOrder) IOrder.State.OPENED else IOrder.State.FILLED)
            {
                if (slDistance == -1.0) BROKER_BUY_OPPOSITE_CLOSE else order.zorroId()
            } else BROKER_BUY_FAIL
            BrokerBuySuccess(returnCode, order.openPrice)
        }
}
