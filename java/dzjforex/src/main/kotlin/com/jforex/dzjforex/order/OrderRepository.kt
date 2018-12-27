package com.jforex.dzjforex.order

import arrow.Kind
import arrow.core.Option
import arrow.core.Try
import arrow.core.orElse
import arrow.effects.ForIO
import com.dukascopy.api.IOrder
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi

typealias OrderId = Int

lateinit var orderRepositoryApi: ContextDependencies<ForIO>

fun initOrderRepositoryApi()
{
    orderRepositoryApi = contextApi
}

fun IOrder.zorroId() = id.toInt()

object OrderRepositoryApi
{
    fun <F> ContextDependencies<F>.getOrderForId(orderId: OrderId): Kind<F, IOrder> =
        getOrderForIdInOpenOrders(orderId)
            .orElse { getOrderForIdInHistoryOrders(orderId) }
            .fromOption { JFException("Order id $orderId not found") }

    fun <F> ContextDependencies<F>.getOrderForIdInOpenOrders(orderId: OrderId): Option<IOrder> =
        getOpenOrders()
            .map { it.first { order -> order.zorroId() == orderId } }
            .toOption()

    fun <F> ContextDependencies<F>.getOrderForIdInHistoryOrders(orderId: OrderId): Option<IOrder> =
        Try { history.getHistoricalOrderById(orderId.toString()) }.toOption()

    fun <F> ContextDependencies<F>.getOpenOrders(): Try<List<IOrder>> = Try { engine.orders }

    fun <F> ContextDependencies<F>.getZorroOrders(): Try<List<IOrder>> =
        getOpenOrders().map { orders -> orders.filter { it.label.startsWith(pluginSettings.labelPrefix()) } }
}

