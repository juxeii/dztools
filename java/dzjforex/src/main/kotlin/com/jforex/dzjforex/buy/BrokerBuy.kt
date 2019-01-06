package com.jforex.dzjforex.buy

import arrow.Kind
import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.createInstrument
import com.jforex.dzjforex.misc.InstrumentApi.filterTradeableInstrument
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.time.BrokerTimeApi.getServerTime
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import com.jforex.dzjforex.zorro.BROKER_BUY_NO_RESPONSE
import com.jforex.dzjforex.zorro.BROKER_BUY_OPPOSITE_CLOSE
import com.jforex.dzjforex.zorro.fillTimeout
import com.jforex.kforexutils.engine.submit
import com.jforex.kforexutils.instrument.ask
import com.jforex.kforexutils.instrument.bid
import com.jforex.kforexutils.misc.asPrice
import com.jforex.kforexutils.order.extension.isCanceled
import com.jforex.kforexutils.order.extension.isCreated
import com.jforex.kforexutils.order.extension.isFilled
import com.jforex.kforexutils.order.extension.isPartiallyFilled
import com.jforex.kforexutils.price.Price
import com.jforex.kforexutils.settings.TradingSettings
import java.util.concurrent.TimeUnit

object BrokerBuyApi
{
    fun <F> ContextDependencies<F>.brokerBuy(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        slippage: Double,
        orderText: String
    ) =
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
            .flatMap { processOrderAndGetResult(it, slDistance) }
            .handleError { error ->
                when (error)
                {
                    is AssetNotTradeableException ->
                    {
                        logger.error("Asset $assetName currently not tradeable!")
                        printOnZorro("Asset $assetName currently not tradeable!")
                    }
                    else -> logger.error(
                        "BrokerBuy failed! Error: ${error.message}" +
                                " Stack trace: ${getStackTrace(error)}"
                    )
                }
                BrokerBuyData(returnCode = BROKER_BUY_FAIL)
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
            val roundedLimitPrice = roundedLimitPrice(instrument, limitPrice)
            val orderCommand = createOrderCommand(contracts, isLimitOrder)
            val goodTillTime = if (isLimitOrder) getServerTime().bind() + fillTimeout * 1000L
            else TradingSettings.defaultGTT
            engine
                .submit(
                    label = createLabel().bind(),
                    instrument = instrument,
                    orderCommand = orderCommand,
                    amount = contracts.toAmount(),
                    stopLossPrice = createSLPrice(slDistance, instrument, orderCommand, limitPrice),
                    price = roundedLimitPrice,
                    slippage = slippage,
                    goodTillTime = goodTillTime,
                    comment = orderText
                )
                .map { it.order }
                .doOnNext { logger.debug("Order update: $it") }
                .takeUntil { it.isFilled || it.isCanceled }
                .take(fillTimeout, TimeUnit.SECONDS)
                .blockingLast()
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

    fun roundedLimitPrice(instrument: Instrument, limitPrice: Double) =
        if (limitPrice != 0.0) Price(instrument, limitPrice).toDouble() else 0.0

    fun <F> ContextDependencies<F>.createSLPrice(
        slDistance: Double,
        instrument: Instrument,
        orderCommand: IEngine.OrderCommand,
        limitPrice: Double
    ): Double
    {
        if (slDistance <= 0) return TradingSettings.noSLPrice
        val openPrice = when
        {
            limitPrice != 0.0 -> limitPrice
            orderCommand == IEngine.OrderCommand.BUY -> instrument.ask()
            else -> instrument.bid()
        }
        val slPrice = if (orderCommand == IEngine.OrderCommand.BUY) openPrice - slDistance
        else openPrice + slDistance
        return slPrice.asPrice(instrument)
    }

    fun <F> ContextDependencies<F>.processOrderAndGetResult(
        order: IOrder,
        slDistance: Double
    ) =
        binding {
            val returnCode = when
            {
                slDistance == -1.0 -> BROKER_BUY_OPPOSITE_CLOSE
                order.isCreated -> BROKER_BUY_NO_RESPONSE
                order.isPartiallyFilled -> BROKER_BUY_FAIL
                else -> order.zorroId()
            }
            val fill = if (order.isPartiallyFilled) order.amount.toSignedContracts(order.orderCommand).toDouble()
            else 0.0
            BrokerBuyData(returnCode = returnCode, price = order.openPrice, fill = fill)
        }
}
