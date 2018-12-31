package com.jforex.dzjforex.stop

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_FAIL
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_OK
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.setSL

object BrokerStopApi
{
    fun <F> ContextDependencies<F>.brokerStop(orderId: Int, slPrice: Double): Kind<F, Int> =
        bindingCatch {
            logger.debug("BrokerStop called orderId $orderId slPrice $slPrice")
            val order =  getOrderForId(orderId).bind()
            logger.debug("BrokerStop order $order")
            val orderEvent = setSLPrice(order, slPrice).bind()
            logger.debug("BrokerStop orderEvent $orderEvent")
            if (orderEvent.type == OrderEventType.CHANGED_SL) BROKER_ADJUST_SL_OK else BROKER_ADJUST_SL_FAIL
        }.handleError { error ->
            logger.error("BrokerStop failed! Error: ${error.message} Stack trace: ${getStackTrace(error)}")
            BROKER_ADJUST_SL_FAIL
        }

    fun <F> ContextDependencies<F>.setSLPrice(order: IOrder, slPrice: Double): Kind<F, OrderEvent> =
        catch {
            order
                .setSL(slPrice = slPrice) {}
                .blockingLast()
        }
}