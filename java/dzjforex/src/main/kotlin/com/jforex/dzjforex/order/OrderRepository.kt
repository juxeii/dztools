package com.jforex.dzjforex.order

import arrow.core.Option
import arrow.core.Try
import arrow.core.or
import arrow.effects.ForIO
import arrow.effects.instances.io.monad.monad
import com.dukascopy.api.IOrder
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForIdInHistoryOrders
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForIdInOpenOrders
import com.jforex.dzjforex.time.BrokerTimeDependencies

typealias OrderId = Int
typealias OrderRepository = Map<OrderId, IOrder>

lateinit var orderRepositoryApi: ContextDependencies

fun initOrderRepositoryApi()
{
    orderRepositoryApi = contextApi
}

val ordersRelay: BehaviorRelay<OrderRepository> = BehaviorRelay.createDefault(emptyMap())

fun getOrders() = ordersRelay.value!!

fun storeOrder(order: IOrder)
{
    ordersRelay.accept(updateOrderRepository(order))
}

fun updateOrderRepository(order: IOrder) = getOrders().plus(Pair(order.id.toInt(), order))

fun IOrder.zorroId() = id.toInt()

object OrderRepositoryApi
{
    fun ContextDependencies.getOrderForId(orderId: OrderId): Option<IOrder>
    {
        logger.debug("Trying to find orderid $orderId")
        val maybeIOrder = Option
            .fromNullable(getOrders()[orderId])
            .or(getOrderForIdInOpenOrders(orderId))
            .or(getOrderForIdInHistoryOrders(orderId))
        logger.debug("orderid $orderId found: ${maybeIOrder.nonEmpty()}")
        return maybeIOrder
    }

    fun ContextDependencies.getOrderForIdInOpenOrders(orderId: OrderId): Option<IOrder> =
        Try {
            logger.debug("Trying to find orderid $orderId in open orders")
            engine.orders.first { order -> order.zorroId() == orderId }
        }.toOption()

    fun ContextDependencies.getOrderForIdInHistoryOrders(orderId: OrderId): Option<IOrder> =
        Try {
            logger.debug("Trying to find orderid $orderId in history orders")
            history.getHistoricalOrderById(orderId.toString())
        }.toOption()

}

