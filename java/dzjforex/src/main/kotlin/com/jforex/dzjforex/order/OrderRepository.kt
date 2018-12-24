package com.jforex.dzjforex.order

import arrow.core.Option
import arrow.core.Try
import arrow.core.orElse
import com.dukascopy.api.IOrder
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.logger

typealias OrderId = Int

lateinit var orderRepositoryApi: ContextDependencies

fun initOrderRepositoryApi()
{
    orderRepositoryApi = contextApi
}

fun IOrder.zorroId() = id.toInt()

object OrderRepositoryApi
{
    fun ContextDependencies.getOrderForId(orderId: OrderId): Option<IOrder>
    {
        logger.debug("Trying to find orderid $orderId")
        val maybeIOrder = getOrderForIdInOpenOrders(orderId).orElse { getOrderForIdInHistoryOrders(orderId) }
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

