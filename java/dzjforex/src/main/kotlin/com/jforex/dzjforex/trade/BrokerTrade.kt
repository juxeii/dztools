package com.jforex.dzjforex.trade

import arrow.Kind
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.OrderLookupApi.getOrderForId
import com.jforex.dzjforex.zorro.BROKER_ORDER_NOT_YET_FILLED
import com.jforex.dzjforex.zorro.BROKER_TRADE_FAIL
import com.jforex.kforexutils.instrument.ask
import com.jforex.kforexutils.instrument.bid
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.order.extension.isFilled
import com.jforex.kforexutils.order.extension.isOpened

object BrokerTradeApi
{
    data class BrokerTradeData(val open: Double, val close: Double, val profit: Double)

    fun <F> ContextDependencies<F>.brokerTrade(
        orderId: Int,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> =
        getOrderForId(orderId)
            .flatMap { order -> processOrder(order, out_TradeInfoToFill) }
            .handleError { error ->
                when (error)
                {
                    is OrderIdNotFoundException ->
                    {
                        logger.error("BrokerTrade: order id ${error.orderId} not found!")
                        printOnZorro("BrokerTrade: order id ${error.orderId} not found!")
                    }
                    else ->
                        logger.error(
                            "BrokerTrade failed! Error message: ${error.message} " +
                                    "Stack trace: ${getStackTrace(error)}"
                        )
                }
                BROKER_TRADE_FAIL
            }

    fun <F> ContextDependencies<F>.processOrder(order: IOrder, out_TradeInfoToFill: DoubleArray) =
        binding {
            val iOpen = 0
            val iClose = 1
            val iProfit = 3
            out_TradeInfoToFill[iOpen] = order.openPrice
            out_TradeInfoToFill[iClose] = quoteForOrder(order).bind()
            out_TradeInfoToFill[iProfit] = order.profitLossInAccountCurrency
            logger.debug(
                "${order.instrument} TradeData: openPrice ${out_TradeInfoToFill[iOpen]}" +
                        " close ${out_TradeInfoToFill[iClose]} " +
                        "profit ${out_TradeInfoToFill[iProfit]}"
            )
            createReturnValue(order).bind()
        }

    fun <F> ContextDependencies<F>.quoteForOrder(order: IOrder) =
        delay {
            val instrument = order.instrument
            if (order.isLong) instrument.bid() else instrument.ask()
        }

    fun <F> ContextDependencies<F>.createReturnValue(order: IOrder) =
        delay {
            with(order) {
                val contracts = amount.toContracts()
                when
                {
                    isFilled -> contracts
                    isOpened -> BROKER_ORDER_NOT_YET_FILLED
                    isClosed -> -contracts
                    else -> BROKER_TRADE_FAIL
                }
            }
        }
}
