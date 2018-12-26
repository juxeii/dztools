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
import com.jforex.dzjforex.zorro.BROKER_ORDER_NOT_YET_FILLED
import com.jforex.dzjforex.zorro.UNKNOWN_ORDER_ID
import com.jforex.kforexutils.misc.toAmount
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.order.extension.isFilled
import com.jforex.kforexutils.order.extension.isOpened

fun createBrokerTradeApi(): BrokerTradeDependencies<ForIO> =
    BrokerTradeDependencies(contextApi, createQuoteProviderApi())

interface BrokerTradeDependencies<F> : ContextDependencies<F>,
    QuoteProviderDependencies
{
    companion object
    {
        operator fun <F> invoke(
            contextDependencies: ContextDependencies<F>,
            quoteProviderDependencies: QuoteProviderDependencies
        ): BrokerTradeDependencies<F> =
            object : BrokerTradeDependencies<F>,
                ContextDependencies<F> by contextDependencies,
                QuoteProviderDependencies by quoteProviderDependencies
            {}
    }
}

object BrokerTradeApi
{
    const val rollOverValue = 0.0

    fun <F> BrokerTradeDependencies<F>.create(
        orderId: Int,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> = binding {
        getOrderForId(orderId)
            .map { order ->
                out_TradeInfoToFill[0] = order.openPrice
                out_TradeInfoToFill[1] = quoteForOrder(order);
                out_TradeInfoToFill[2] = rollOverValue
                out_TradeInfoToFill[3] = order.profitLossInAccountCurrency.toAmount()
                createReturnValue(order)
            }.fold({
                logger.debug("BrokerTrade: Id $orderId not found!")
                UNKNOWN_ORDER_ID
            }) {returnValue-> returnValue }
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
            order.isOpened -> BROKER_ORDER_NOT_YET_FILLED
            order.isClosed -> -contracts
            else -> UNKNOWN_ORDER_ID
        }
    }
}
