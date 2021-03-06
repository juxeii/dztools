package com.jforex.dzjforex.order

import arrow.Kind
import arrow.core.Option
import arrow.core.Try
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.OrderId
import com.jforex.dzjforex.misc.OrderIdNotFoundException
import com.jforex.dzjforex.misc.PluginApi.filterTradeableInstrument

object OrderLookupApi
{

    fun <F> ContextDependencies<F>.getOrderForId(orderId: OrderId) =
        getOrderForIdInOpenOrders(orderId).handleErrorWith { getOrderForIdInHistoryOrders(orderId) }

    fun <F> ContextDependencies<F>.getTradeableOrderForId(orderId: OrderId) =
        getOrderForId(orderId).flatMap { order -> filterTradeableOrder(order) }

    fun <F> ContextDependencies<F>.getOrderForIdInOpenOrders(orderId: OrderId) =
        getOpenOrders().flatMap { openOrders -> filterOrderId(orderId, openOrders) }

    fun <F> ContextDependencies<F>.getOrderForIdInHistoryOrders(orderId: OrderId) = Try {
        val order = history.getHistoricalOrderById(orderId.toString())
        order ?: throw OrderIdNotFoundException(orderId)
    }.fromTry { it }

    fun <F> ContextDependencies<F>.getOpenOrders() = Try { engine.orders }.fromTry { it }

    fun <F> ContextDependencies<F>.filterOrderId(orderId: OrderId, orders: List<IOrder>): Kind<F, IOrder> =
        Option
            .fromNullable(orders.firstOrNull { order -> order.zorroId() == orderId })
            .fold({ raiseError(OrderIdNotFoundException(orderId)) }) { just(it) }

    fun <F> ContextDependencies<F>.filterTradeableOrder(order: IOrder) =
        filterTradeableInstrument(order.instrument).map { order }

    fun <F> ContextDependencies<F>.filterZorroOrder(order: IOrder) =
        filterTradeableInstrument(order.instrument).map { order }

    fun <F> ContextDependencies<F>.getOpenZorroOrders() = bindingCatch {
        getOpenOrders()
            .bind()
            .filter { it.isFromZorro() }
    }

    fun <F> ContextDependencies<F>.getOpenZorroOrdersForInstrument(instrument: Instrument) = bindingCatch {
        getOpenZorroOrders()
            .bind()
            .filter { it.instrument == instrument }
    }

}

