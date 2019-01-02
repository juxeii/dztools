package com.jforex.dzjforex.trade

import arrow.Kind
import arrow.effects.ForIO
import arrow.typeclasses.binding
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.QuotesProviderApi.getAsk
import com.jforex.dzjforex.misc.QuotesProviderApi.getBid
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.zorro.BROKER_ORDER_NOT_YET_FILLED
import com.jforex.dzjforex.zorro.BROKER_TRADE_FAIL
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.order.extension.isFilled
import com.jforex.kforexutils.order.extension.isOpened

data class BrokerTradeData(
    val open: Double,
    val close: Double,
    val profit: Double
)

sealed class BrokerTradeResult(val returnCode: Int)
{
    data class Failure(val code: Int) : BrokerTradeResult(code)
    data class Success(val code: Int, val data: BrokerTradeData) : BrokerTradeResult(code)
}
typealias BrokerTradeFailure = BrokerTradeResult.Failure
typealias BrokerTradeSuccess = BrokerTradeResult.Success

fun createBrokerTradeApi(): QuoteDependencies<ForIO> = createQuoteApi(contextApi.jfContext)

object BrokerTradeApi
{
    fun <F> QuoteDependencies<F>.brokerTrade(orderId: Int): Kind<F, BrokerTradeResult> =
        getOrderForId(orderId)
            .flatMap { order ->
                binding {
                    val tradeData = createTradeData(order).bind()
                    val returnCode = createReturnValue(order).bind()
                    BrokerTradeSuccess(returnCode, tradeData)
                }
            }
            .handleError { error ->
                when (error)
                {
                    is OrderIdNotFoundException ->
                        natives.jcallback_BrokerError("BrokerTrade: order id ${error.orderId} not found!")
                    else ->
                        logger.error("BrokerTrade failed! Error: $error Stack trace: ${getStackTrace(error)}")
                }
                BrokerTradeFailure(BROKER_TRADE_FAIL)
            }

    fun <F> QuoteDependencies<F>.createTradeData(order: IOrder): Kind<F, BrokerTradeData> =
        binding {
            val tradeData = BrokerTradeData(
                open = order.openPrice,
                close = quoteForOrder(order).bind(),
                profit = order.profitLossInAccountCurrency
            )
            logger.debug("TradeData: $tradeData OrderData $order order state ${order.state}")
            tradeData
        }

    fun <F> QuoteDependencies<F>.quoteForOrder(order: IOrder): Kind<F, Double> =
        invoke {
            val instrument = order.instrument
            if (order.isLong) getBid(instrument) else getAsk(instrument)
        }

    fun <F> QuoteDependencies<F>.createReturnValue(order: IOrder): Kind<F, Int> =
        invoke {
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
