package com.jforex.dzjforex.trade

import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.OrderIdNotFoundException
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.OrderLookupApi.getOrderForId
import com.jforex.dzjforex.zorro.BROKER_ORDER_NOT_YET_FILLED
import com.jforex.dzjforex.zorro.BROKER_TRADE_FAIL
import com.jforex.kforexutils.instrument.ask
import com.jforex.kforexutils.instrument.bid
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.order.extension.isFilled
import com.jforex.kforexutils.order.extension.isOpened

object BrokerTradeApi {
    fun <F> ContextDependencies<F>.brokerTrade(orderId: Int) =
        getOrderForId(orderId)
            .flatMap { order -> processOrder(order) }
            .handleErrorWith { error -> processError(error) }

    fun <F> ContextDependencies<F>.processError(error: Throwable) = delay {
        when (error) {
            is OrderIdNotFoundException ->
                natives.logAndPrintErrorOnZorro("BrokerTrade: order id ${error.orderId} not found!")
            else ->
                logger.error(
                    "BrokerTrade failed! Error message: ${error.message} " +
                            "Stack trace: ${getStackTrace(error)}"
                )
        }
        BrokerTradeData(BROKER_TRADE_FAIL)
    }

    fun <F> ContextDependencies<F>.processOrder(order: IOrder) = binding {
        BrokerTradeData(
            returnCode = createReturnValue(order).bind(),
            open = order.openPrice,
            close = quoteForOrder(order).bind(),
            profit = order.profitLossInAccountCurrency
        )
    }

    fun <F> ContextDependencies<F>.quoteForOrder(order: IOrder) = delay {
        val instrument = order.instrument
        if (order.isLong) instrument.bid() else instrument.ask()
    }

    fun <F> ContextDependencies<F>.createReturnValue(order: IOrder) = delay {
        with(order) {
            val contracts = amount.toContracts()
            when {
                isFilled -> contracts
                isOpened -> BROKER_ORDER_NOT_YET_FILLED
                isClosed -> -contracts
                else -> BROKER_TRADE_FAIL
            }
        }
    }
}
