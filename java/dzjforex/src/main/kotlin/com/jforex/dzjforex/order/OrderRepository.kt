package com.jforex.dzjforex.order

import arrow.core.Option
import arrow.core.Try
import arrow.core.orElse
import arrow.effects.ForIO
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.OrderRepositoryApi.getOpenOrders
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForIdInHistoryOrders
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForIdInOpenOrders

typealias OrderId = Int

lateinit var orderRepositoryApi: ContextDependencies<ForIO>

fun initOrderRepositoryApi()
{
    orderRepositoryApi = contextApi
}

fun IOrder.zorroId() = id.toInt()

object OrderRepositoryApi
{
    fun <F> ContextDependencies<F>.getOrderForId(orderId: OrderId): Option<IOrder>
    {
        val maybeIOrder = getOrderForIdInOpenOrders(orderId).orElse { getOrderForIdInHistoryOrders(orderId) }
        return maybeIOrder
    }

    fun <F> ContextDependencies<F>.getOrderForIdInOpenOrders(orderId: OrderId): Option<IOrder> =
        getOpenOrders()
            .map { it.first { order -> order.zorroId() == orderId } }
            .toOption()

    fun <F> ContextDependencies<F>.getOrderForIdInHistoryOrders(orderId: OrderId): Option<IOrder> =
        Try {
            //logger.debug("Trying to find orderid $orderId in history orders")
            history.getHistoricalOrderById(orderId.toString())
        }.toOption()

    fun <F> ContextDependencies<F>.getOpenOrders(): Try<List<IOrder>> = Try { engine.orders }

    fun <F> ContextDependencies<F>.getZorroOrders(): Try<List<IOrder>> =
        getOpenOrders().map { orders-> orders.filter { it.label.startsWith(pluginSettings.labelPrefix()) } }
}

