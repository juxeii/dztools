package com.jforex.dzjforex.stop

import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.AssetNotTradeableException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.OrderLookupApi.getTradeableOrderForId
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_FAIL
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_OK
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.setSL

object BrokerStopApi {
    fun <F> ContextDependencies<F>.brokerStop(orderId: Int, slPrice: Double) =
        getTradeableOrderForId(orderId)
            .flatMap { order ->
                logger.debug("BrokerStop: setting stop loss price $slPrice for oder $order")
                setSLPrice(order, slPrice)
            }
            .map(::evaluateCloseEvent)
            .handleErrorWith { error -> processError(error) }

    private fun <F> ContextDependencies<F>.processError(error: Throwable) = delay {
        when (error) {
            is AssetNotTradeableException ->
                natives.logAndPrintErrorOnZorro("BrokerStop: ${error.instrument} currently not tradeable!")
            else ->
                logger.error(
                    "BrokerStop failed! Error: ${error.message} " +
                            "Stack trace: ${getStackTrace(error)}"
                )
        }
        BROKER_ADJUST_SL_FAIL
    }

    fun evaluateCloseEvent(orderEvent: OrderEvent) =
        if (orderEvent.type == OrderEventType.CHANGED_SL) {
            logger.debug("BrokerStop: stop loss price successfully set for order ${orderEvent.order}")
            BROKER_ADJUST_SL_OK
        } else {
            logger.debug("BrokerStop: setting stop loss failed for order ${orderEvent.order} event $orderEvent")
            BROKER_ADJUST_SL_FAIL
        }

    fun <F> ContextDependencies<F>.setSLPrice(order: IOrder, slPrice: Double) = catch {
        order
            .setSL(slPrice = slPrice) {}
            .blockingLast()
    }
}