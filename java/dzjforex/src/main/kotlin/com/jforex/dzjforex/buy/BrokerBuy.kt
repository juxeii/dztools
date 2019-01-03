package com.jforex.dzjforex.buy

import arrow.Kind
import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.createInstrument
import com.jforex.dzjforex.misc.InstrumentApi.filterTradeableInstrument
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import com.jforex.dzjforex.zorro.BROKER_BUY_OPPOSITE_CLOSE
import com.jforex.dzjforex.zorro.BROKER_BUY_TIMEOUT
import com.jforex.kforexutils.engine.submit
import com.jforex.kforexutils.instrument.ask
import com.jforex.kforexutils.instrument.bid
import com.jforex.kforexutils.misc.asPrice
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.price.Price
import com.jforex.kforexutils.settings.TradingSettings
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object BrokerBuyApi
{
    fun <F> ContextDependencies<F>.brokerBuy(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        slippage: Double,
        orderText: String,
        out_BuyInfoToFill: DoubleArray
    ): Kind<F, Int> =
        createInstrument(assetName)
            .flatMap { instrument -> filterTradeableInstrument(instrument) }
            .flatMap { instrument ->
                submitOrder(
                    instrument = instrument,
                    contracts = contracts,
                    slDistance = slDistance,
                    limitPrice = limitPrice,
                    slippage = slippage,
                    orderText = orderText
                )
            }
            .flatMap { processOrderAndGetResult(it, slDistance, limitPrice != 0.0, out_BuyInfoToFill) }
            .handleError { error ->
                when (error)
                {
                    is AssetNotTradeableException ->
                    {
                        logger.error("Asset $assetName currently not tradeable!")
                        printOnZorro("Asset $assetName currently not tradeable!")
                    }
                    is TimeoutException ->
                    {
                        logger.error("BrokerBuy timeout for $assetName!")
                        printOnZorro("BrokerBuy timeout for $assetName!")
                    }
                    else -> logger.error(
                        "BrokerBuy failed! Error: ${error.message}" +
                                " Stack trace: ${getStackTrace(error)}"
                    )
                }
                if (error is TimeoutException) BROKER_BUY_TIMEOUT else BROKER_BUY_FAIL
            }

    private fun <F> ContextDependencies<F>.submitOrder(
        instrument: Instrument,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        slippage: Double,
        orderText: String
    ): Kind<F, IOrder> =
        binding {
            val isLimitOrder = limitPrice != 0.0
            val roundedLimitPrice = createLimitPrice(instrument, limitPrice)
            val orderCommand = createOrderCommand(contracts, isLimitOrder)
            engine
                .submit(
                    label = createLabel().bind(),
                    instrument = instrument,
                    orderCommand = orderCommand,
                    amount = contracts.toAmount(),
                    stopLossPrice = createSLPrice(slDistance, instrument, orderCommand, limitPrice),
                    price = roundedLimitPrice,
                    slippage = slippage,
                    comment = orderText
                )
                .filter {
                    if (roundedLimitPrice != 0.0) it.type == OrderEventType.SUBMIT_OK
                    else it.type == OrderEventType.FULLY_FILLED
                }
                .timeout(pluginSettings.maxSecondsForOrderBuy(), TimeUnit.SECONDS)
                .map { it.order }
                .blockingFirst()
        }

    fun <F> ContextDependencies<F>.createLabel(): Kind<F, String> =
        delay { pluginSettings.labelPrefix() + System.currentTimeMillis().toString() }

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

    fun <F> ContextDependencies<F>.createSLPrice(
        slDistance: Double,
        instrument: Instrument,
        orderCommand: IEngine.OrderCommand,
        limitPrice: Double
    ): Double
    {
        if (slDistance <= 0) return TradingSettings.noSLPrice
        val openPrice = if (limitPrice != 0.0) limitPrice
        else if (orderCommand == IEngine.OrderCommand.BUY) instrument.ask() else instrument.bid()
        val slPrice = if (orderCommand == IEngine.OrderCommand.BUY) openPrice - slDistance
        else openPrice + slDistance
        return slPrice.asPrice(instrument)
    }

    fun <F> ContextDependencies<F>.processOrderAndGetResult(
        order: IOrder,
        slDistance: Double,
        isLimitOrder: Boolean,
        out_BuyInfoToFill: DoubleArray
    ): Kind<F, Int> =
        binding {
            fillBuyData(out_BuyInfoToFill, order.openPrice).bind()
            val returnCode = if (order.state == if (isLimitOrder) IOrder.State.OPENED else IOrder.State.FILLED)
            {
                if (slDistance == -1.0) BROKER_BUY_OPPOSITE_CLOSE else order.zorroId()
            } else BROKER_BUY_FAIL
            returnCode
        }

    fun <F> ContextDependencies<F>.fillBuyData(out_BuyInfoToFill: DoubleArray, price: Double) = delay {
        val iPrice = 0
        out_BuyInfoToFill[iPrice] = price
    }
}
