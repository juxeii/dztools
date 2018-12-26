package com.jforex.dzjforex.stop

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.stop.BrokerStopApi.setSLPrice
import com.jforex.dzjforex.zorro.ADJUST_SL_FAIL
import com.jforex.dzjforex.zorro.ADJUST_SL_OK
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.setSL

object BrokerStopApi
{
    fun <F> ContextDependencies<F>.setSL(orderId: Int, slPrice: Double): Kind<F, Int> =
        bindingCatch {
            getOrderForId(orderId)
                .map { order -> setSLPrice(order, slPrice).bind() }
                .fold({ ADJUST_SL_FAIL })
                { orderEvent ->
                    if (orderEvent.type == OrderEventType.CHANGED_SL) ADJUST_SL_OK else ADJUST_SL_FAIL
                }
        }

    fun <F> ContextDependencies<F>.setSLPrice(order: IOrder, slPrice: Double): Kind<F, OrderEvent> =
        catch {
            order
                .setSL(slPrice = slPrice) {}
                .blockingLast()
        }
}