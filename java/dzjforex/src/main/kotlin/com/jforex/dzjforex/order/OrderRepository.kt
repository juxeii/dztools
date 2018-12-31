package com.jforex.dzjforex.order

import arrow.Kind
import arrow.core.None
import arrow.core.Option
import arrow.core.Try
import arrow.core.orElse
import arrow.effects.ForIO
import com.dukascopy.api.IOrder
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.logger
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

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

    fun <F> ContextDependencies<F>.getOrderForIdInOpenOrders(orderId: OrderId): Option<IOrder>
    {
        logger.debug("Seeking orderID $orderId")
        return getOpenOrders()
            .map { openOrders ->
                logger.debug("Found open orders $openOrders")
                filterOrderId(orderId, openOrders)
            }
            .fold({
                logger.debug("open orders failed with ${it.message}")
                None
            }, {
                logger.debug("open orders found order $it")
                it
            })
    }

    fun <F> ContextDependencies<F>.getOrderForIdInHistoryOrders(orderId: OrderId): Option<IOrder> =
        Try {
            logger.debug("Seeking orderId $orderId in history orders orderId.toString() ${orderId.toString()}")
            Observable.interval(
                0,
                pluginSettings.historyAccessRetryDelay(),
                TimeUnit.MILLISECONDS
            )
                .take(pluginSettings.historyAccessRetries())
                .map {
                    logger.debug("Seeking orderId TRY $it")
                    val order = history.getHistoricalOrderById(orderId.toString())
                    logger.debug("Seeking TRY $it found $order")
                    Option.fromNullable(order)
                }
                .filter {
                    !it.isEmpty()
                }
                .doOnError {
                    logger.debug("Error in seeking order id ${it.message}")
                }
                .blockingFirst()


            //history.getHistoricalOrderById(orderId.toString())
        }.fold({ None }, { it })

    fun <F> ContextDependencies<F>.getOpenOrders(): Try<List<IOrder>> = Try { engine.orders }

    fun filterOrderId(orderId: OrderId, orders: List<IOrder>): Option<IOrder> =
        Option.fromNullable(orders.firstOrNull { order -> order.zorroId() == orderId })

    fun <F> ContextDependencies<F>.getZorroOrders(): Try<List<IOrder>> =
        getOpenOrders().map { orders -> orders.filter { it.label.startsWith(pluginSettings.labelPrefix()) } }
}

