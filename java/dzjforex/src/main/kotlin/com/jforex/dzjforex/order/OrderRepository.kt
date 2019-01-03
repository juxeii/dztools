package com.jforex.dzjforex.order

import arrow.Kind
import arrow.core.Option
import arrow.core.Try
import arrow.effects.ForIO
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InstrumentApi.filterTradeableInstrument
import com.jforex.dzjforex.misc.OrderIdNotFoundException
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.logger

typealias OrderId = Int

lateinit var orderRepositoryApi: ContextDependencies<ForIO>

fun initOrderRepositoryApi()
{
    orderRepositoryApi = contextApi
}

fun IOrder.zorroId() = id.toInt()

object OrderLookupApi
{
    fun <F> ContextDependencies<F>.getOrderForId(orderId: OrderId): Kind<F, IOrder> =
        getOrderForIdInOpenOrders(orderId).handleErrorWith { getOrderForIdInHistoryOrders(orderId) }

    fun <F> ContextDependencies<F>.getTradeableOrderForId(orderId: OrderId): Kind<F, IOrder> =
        getOrderForId(orderId).flatMap { order -> filterTradeableOrder(order) }

    fun <F> ContextDependencies<F>.getOrderForIdInOpenOrders(orderId: OrderId): Kind<F, IOrder>
    {
        logger.debug("Seeking orderID $orderId in open orders")
        return getOpenOrders().flatMap { openOrders -> filterOrderId(orderId, openOrders) }
    }

    fun <F> ContextDependencies<F>.getOrderForIdInHistoryOrders(orderId: OrderId): Kind<F, IOrder> =
        Try {
            logger.debug("Seeking orderId $orderId in history orders orderId.toString() ${orderId.toString()}")
            val order = history.getHistoricalOrderById(orderId.toString())
            if (order == null)
            {
                logger.debug("No order for id $orderId in history!")
                throw OrderIdNotFoundException(orderId)
            }
            logger.debug("Found orderId $orderId in history!")
            order
        }.fromTry { it }

    fun <F> ContextDependencies<F>.getOpenOrders(): Kind<F, List<IOrder>> = Try { engine.orders }.fromTry { it }

    fun <F> ContextDependencies<F>.filterOrderId(orderId: OrderId, orders: List<IOrder>): Kind<F, IOrder> =
        Option
            .fromNullable(orders.firstOrNull { order -> order.zorroId() == orderId })
            .fold({ raiseError(OrderIdNotFoundException(orderId)) }) { just(it) }

    /*fun <F> ContextDependencies<F>.getZorroOrders(): Try<List<IOrder>> =
        getOpenOrders().map { orders -> orders.filter { it.label.startsWith(pluginSettings.labelPrefix()) } }*/

    fun <F> ContextDependencies<F>.filterTradeableOrder(order: IOrder): Kind<F, IOrder> =
        filterTradeableInstrument(order.instrument).map { order }
}

