package com.jforex.dzjforex.buy

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.PluginApi.contractsToAmount
import com.jforex.dzjforex.misc.QuotesApi.getAsk
import com.jforex.dzjforex.misc.QuotesApi.getBid
import com.jforex.dzjforex.order.storeOrder
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import com.jforex.dzjforex.zorro.BROKER_BUY_OPPOSITE_CLOSE
import com.jforex.dzjforex.zorro.BROKER_BUY_TIMEOUT
import com.jforex.kforexutils.engine.submit
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.price.Pips
import com.jforex.kforexutils.price.Price
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

lateinit var brokerBuyApi: BrokerBuyDependencies<ForIO>

fun initBrokerBuyApi()
{
    brokerBuyApi = BrokerBuyDependencies(pluginApi, contextApi, createQuoteProviderApi(), IO.monadError())
}

data class BuyParameter(
    val label: String,
    val instrument: Instrument,
    val orderCommand: IEngine.OrderCommand,
    val amount: Double,
    val slPrice: Double,
    val price: Double
)

interface BrokerBuyDependencies<F> : PluginDependencies,
    ContextDependencies,
    QuoteProviderDependencies,
    InstrumentFunc<F>,
    MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            quoteProviderDependencies: QuoteProviderDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerBuyDependencies<F> =
            object : BrokerBuyDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                QuoteProviderDependencies by quoteProviderDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerBuyApi
{
    fun <F> BrokerBuyDependencies<F>.create(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> =
        createBuyParameter(
            assetName = assetName,
            contracts = contracts,
            slDistance = slDistance,
            limitPrice = limitPrice
        )
            .flatMap { submitOrder(it) }
            .flatMap { processOrderAndGetResult(it, slDistance, out_TradeInfoToFill) }
            .handleError { error ->
                if (error is TimeoutException) BROKER_BUY_TIMEOUT else BROKER_BUY_FAIL
            }

    fun <F> BrokerBuyDependencies<F>.createBuyParameter(
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
        val price = if (isLimitOrder) limitPrice else 0.0
        val slPrice = createSLPrice(slDistance, instrument, orderCommand, limitPrice)

        BuyParameter(
            label = label,
            instrument = instrument,
            orderCommand = orderCommand,
            amount = amount,
            slPrice = slPrice,
            price = price
        )
    }

    fun <F> BrokerBuyDependencies<F>.submitOrder(buyParameter: BuyParameter): Kind<F, IOrder> =
        catch {
            engine
                .submit(
                    label = buyParameter.label,
                    instrument = buyParameter.instrument,
                    orderCommand = buyParameter.orderCommand,
                    amount = buyParameter.amount,
                    stopLossPrice = buyParameter.slPrice,
                    price = buyParameter.price
                )
                .filter { it.type == OrderEventType.FULLY_FILLED }
                .timeout(pluginSettings.maxSecondsForOrderFill(), TimeUnit.SECONDS)
                .map { it.order }
                .blockingFirst()
        }

    fun <F> BrokerBuyDependencies<F>.createLabel(): Kind<F, String> =
        just(pluginSettings.labelPrefix() + System.currentTimeMillis().toString())

    fun createOrderCommand(contracts: Int, isLimitOrder: Boolean): IEngine.OrderCommand =
        when
        {
            contracts >= 0 && !isLimitOrder -> IEngine.OrderCommand.BUY
            contracts >= 0 && isLimitOrder -> IEngine.OrderCommand.BUYLIMIT
            contracts < 0 && !isLimitOrder -> IEngine.OrderCommand.SELL
            else -> IEngine.OrderCommand.SELLLIMIT
        }

    fun <F> BrokerBuyDependencies<F>.createSLPrice(
        slDistance: Double,
        instrument: Instrument,
        orderCommand: IEngine.OrderCommand,
        limitPrice: Double
    ): Double
    {
        if (slDistance <= 0) return 0.0
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

    fun <F> BrokerBuyDependencies<F>.processOrderAndGetResult(
        order: IOrder,
        slDistance: Double,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> =
        catch {
            if (order.state == IOrder.State.FILLED)
            {
                storeOrder(order)
                out_TradeInfoToFill[0] = order.openPrice
                if (slDistance == -1.0) BROKER_BUY_OPPOSITE_CLOSE else order.zorroId()
            } else BROKER_BUY_FAIL
        }
}