package com.jforex.dzjforex.stop

import arrow.Kind
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.AssetNotTradeableException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.OrderRepositoryApi.getTradeableOrderForId
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_FAIL
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_OK
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.setSL

object BrokerStopApi
{
    fun <F> ContextDependencies<F>.brokerStop(orderId: Int, slPrice: Double): Kind<F, Int> =
        getTradeableOrderForId(orderId)
            .flatMap { order -> setSLPrice(order, slPrice) }
            .map { orderEvent ->
                if (orderEvent.type == OrderEventType.CHANGED_SL) BROKER_ADJUST_SL_OK
                else BROKER_ADJUST_SL_FAIL
            }.handleError { error ->
                when (error)
                {
                    is AssetNotTradeableException ->
                        natives.jcallback_BrokerError("BrokerStop: asset ${error.instrument} currently not tradeable!")
                    else ->
                        logger.error("BrokerStop failed! Error: ${error.message} Stack trace: ${getStackTrace(error)}")
                }
                BROKER_ADJUST_SL_FAIL
            }

    fun <F> ContextDependencies<F>.setSLPrice(order: IOrder, slPrice: Double): Kind<F, OrderEvent> =
        catch {
            order
                .setSL(slPrice = slPrice) {}
                .blockingLast()
        }
}