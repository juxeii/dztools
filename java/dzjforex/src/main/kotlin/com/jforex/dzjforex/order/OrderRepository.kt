package com.jforex.dzjforex.order

import arrow.core.Option
import com.dukascopy.api.IOrder
import com.jakewharton.rxrelay2.BehaviorRelay

typealias OrderId = Int
typealias OrderRepository = Map<OrderId, IOrder>

val ordersRelay: BehaviorRelay<OrderRepository> = BehaviorRelay.createDefault(emptyMap())

fun getOrders() = ordersRelay.value!!

fun storeOrder(order: IOrder)
{
    ordersRelay.accept(updateOrderRepository(order))
}

fun updateOrderRepository(order: IOrder) = getOrders().plus(Pair(order.id.toInt(), order))

fun getOrderForId(orderId: OrderId) = Option.fromNullable(getOrders()[orderId])
