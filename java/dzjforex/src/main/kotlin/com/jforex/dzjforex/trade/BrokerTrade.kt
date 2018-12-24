package com.jforex.dzjforex.trade

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.binding
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.PluginApi.amountToContracts
import com.jforex.dzjforex.misc.QuotesApi.getAsk
import com.jforex.dzjforex.misc.QuotesApi.getBid
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.zorro.UNKNOWN_ORDER_ID
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.order.extension.isFilled

fun createBrokerTradeApi(): BrokerTradeDependencies<ForIO> =
    BrokerTradeDependencies(pluginApi, contextApi, createQuoteProviderApi(), IO.monadError())

interface BrokerTradeDependencies<F> : PluginDependencies,
    ContextDependencies,
    QuoteProviderDependencies,
    MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            quoteProviderDependencies: QuoteProviderDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerTradeDependencies<F> =
            object : BrokerTradeDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                QuoteProviderDependencies by quoteProviderDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerTradeApi
{
    fun <F> BrokerTradeDependencies<F>.create(
        orderId: Int,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> = binding {
        getOrderForId(orderId)
            .map { order ->
                out_TradeInfoToFill[0] = order.openPrice
                out_TradeInfoToFill[1] = quoteForOrder(order);
                out_TradeInfoToFill[2] = 0.0 //Rollover not supported
                out_TradeInfoToFill[3] = order.profitLossInAccountCurrency
                createReturnValue(order)
            }.fold({
                logger.debug("BrokerTrade: Id $orderId not found!")
                UNKNOWN_ORDER_ID
            }) {returnValue->
                logger.debug("BrokerTrade successful. returnValue $returnValue")
                    returnValue
            }
    }

    fun <F> BrokerTradeDependencies<F>.quoteForOrder(order: IOrder): Double
    {
        val instrument = order.instrument
        return if (order.isLong) getBid(instrument) else getAsk(instrument)
    }

    fun <F> BrokerTradeDependencies<F>.createReturnValue(order: IOrder): Int
    {
        val contracts = amountToContracts(order.amount)
        return when
        {
            order.isFilled -> contracts
            order.isClosed -> -contracts
            else -> UNKNOWN_ORDER_ID
        }
    }
}
