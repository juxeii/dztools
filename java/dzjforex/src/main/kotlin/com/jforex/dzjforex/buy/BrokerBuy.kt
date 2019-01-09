package com.jforex.dzjforex.buy

import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountApi.isNFAAccount
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.PluginApi.createInstrument
import com.jforex.dzjforex.misc.PluginApi.filterTradeableInstrument
import com.jforex.dzjforex.time.BrokerTimeApi.getServerTime
import com.jforex.dzjforex.zorro.*
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
    ) = createInstrument(assetName)
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
        .handleErrorWith { error -> processError(error) }

    fun <F> ContextDependencies<F>.processError(error: Throwable) = delay {
        when (error)
        {
            is InvalidAssetNameException ->
                natives.logAndPrintErrorOnZorro("BrokerBuy: Asset name ${error.assetName} is invalid!")
            is AssetNotTradeableException ->
                natives.logAndPrintErrorOnZorro("BrokerBuy: Asset ${error.instrument} currently not tradeable!")
            else -> logger.error(
                "BrokerBuy failed! Error: ${error.message}" +
                        " Stack trace: ${getStackTrace(error)}"
            )
        }
        BrokerBuyData(BROKER_BUY_FAIL)
    }

    private fun <F> ContextDependencies<F>.submitOrder(
        instrument: Instrument,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        slippage: Double,
        orderText: String
    ) = bindingCatch {
        val isLimitOrder = isLimitOrder(limitPrice)
        val orderCommand = createOrderCommand(contracts, isLimitOrder)
        engine
            .submit(
                label = createLabel().bind(),
                instrument = instrument,
                orderCommand = orderCommand,
                amount = contracts.toAmount(),
                stopLossPrice = createSLPrice(slDistance, instrument, orderCommand, limitPrice),
                price = roundedLimitPrice(instrument, limitPrice),
                slippage = slippage,
                goodTillTime = createGTT(limitPrice).bind(),
                comment = orderText
            )
            .map { it.order }
            .doOnNext { logger.debug("Brokerbuy: Order update: $it") }
            .takeUntil { it.isFilled || it.isCanceled }
            .take(fillTimeout, TimeUnit.SECONDS)
            .blockingLast()
    }

    fun isLimitOrder(limitPrice: Double) = limitPrice != 0.0

    fun <F> ContextDependencies<F>.createGTT(limitPrice: Double) = binding {
        if (isLimitOrder(limitPrice)) getServerTime().bind() + fillTimeout * 1000L
        else TradingSettings.defaultGTT
    }

    fun <F> ContextDependencies<F>.createLabel() = delay {
        pluginSettings.labelPrefix() + System.currentTimeMillis().toString()
    }

    fun createOrderCommand(contracts: Int, isLimitOrder: Boolean): IEngine.OrderCommand = when
    {
        contracts >= 0 && !isLimitOrder -> IEngine.OrderCommand.BUY
        contracts >= 0 && isLimitOrder -> IEngine.OrderCommand.BUYLIMIT
        contracts < 0 && !isLimitOrder -> IEngine.OrderCommand.SELL
        else -> IEngine.OrderCommand.SELLLIMIT
    }

    fun roundedLimitPrice(instrument: Instrument, limitPrice: Double) =
        if (isLimitOrder(limitPrice)) Price(instrument, limitPrice).toDouble() else 0.0

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
            isLimitOrder(limitPrice) -> limitPrice
            orderCommand == IEngine.OrderCommand.BUY -> instrument.ask()
            else -> instrument.bid()
        }
        val slPrice = if (orderCommand == IEngine.OrderCommand.BUY) openPrice - slDistance
        else openPrice + slDistance
        return slPrice.asPrice(instrument)
    }

    fun <F> ContextDependencies<F>.processOrderAndGetResult(order: IOrder, slDistance: Double) = binding {
        when
        {
            order.isCreated -> BrokerBuyData(BROKER_BUY_NO_RESPONSE)
            order.isPartiallyFilled ->
            {
                if (!isNFAAccount().bind()) order.close()
                BrokerBuyData(
                    returnCode = BROKER_BUY_FILL_TIMEOUT,
                    price = order.openPrice,
                    fill = order.toSignedContracts().toDouble()
                )
            }
            slDistance == -1.0 -> BrokerBuyData(
                returnCode = BROKER_BUY_OPPOSITE_CLOSE,
                price = order.openPrice
            )
            else -> BrokerBuyData(
                returnCode = order.zorroId(),
                price = order.openPrice
            )
        }
    }
}
