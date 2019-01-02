package com.jforex.dzjforex.sell

import arrow.Kind
import arrow.core.Option
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.OrderRepositoryApi.getTradeableOrderForId
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.zorro.BROKER_SELL_FAIL
import com.jforex.kforexutils.misc.asPrice
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.close
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.settings.TradingSettings

object BrokerSellApi
{
    private data class CloseParams(val order: IOrder, val amount: Double, val price: Double, val slippage: Double)

    fun <F> ContextDependencies<F>.brokerSell(
        orderId: Int,
        contracts: Int,
        maybeLimitPrice: Option<Double>,
        slippage: Double
    ): Kind<F, Int> =
        getTradeableOrderForId(orderId)
            .flatMap { order ->
                if (order.isClosed)
                {
                    logger.warn("BrokerSell Trying to close already closed order! $order state ${order.state}")
                    just(order.zorroId())
                } else
                {
                    logger.debug("BrokerSell called orderId $orderId contracts $contracts order $order")
                    val closeParams = CloseParams(
                        order = order,
                        amount = contracts.toAmount(),
                        price = getLimitPrice(maybeLimitPrice, order.instrument),
                        slippage = slippage
                    )
                    closeOrder(closeParams).map { orderEvent -> processOrderEventAndGetResult(orderEvent) }
                }
            }.handleError { error ->
                logError(error)
                BROKER_SELL_FAIL
            }

    private fun <F> ContextDependencies<F>.logError(error: Throwable): Kind<F, Unit> = invoke {
        when (error)
        {
            is OrderIdNotFoundException ->
            {
                logger.error("BrokerSell: orderId ${error.orderId} not found!")
                printOnZorro("BrokerSell: orderId ${error.orderId} not found!")
            }
            is AssetNotTradeableException ->
            {
                logger.error("BrokerSell: asset ${error.instrument} currently not tradeable!")
                printOnZorro("BrokerSell: asset ${error.instrument} currently not tradeable!")
            }
            else ->
                logger.error("BrokerSell failed! Error: ${error.message} Stack trace: ${getStackTrace(error)}")
        }
    }

    private fun <F> ContextDependencies<F>.closeOrder(closeParams: CloseParams): Kind<F, OrderEvent> =
        invoke {
            closeParams
                .order
                .close(
                    amount = closeParams.amount,
                    price = closeParams.price,
                    slippage = closeParams.slippage
                ) {}
                .blockingLast()
        }

    private fun processOrderEventAndGetResult(orderEvent: OrderEvent): Int =
        if (orderEvent.type == OrderEventType.CLOSE_OK || orderEvent.type == OrderEventType.PARTIAL_CLOSE_OK)
            orderEvent.order.zorroId()
        else BROKER_SELL_FAIL

    private fun getLimitPrice(maybeLimitPrice: Option<Double>, instrument: Instrument) =
        maybeLimitPrice
            .fold({ TradingSettings.noPreferredClosePrice })
            { limitPrice -> limitPrice.asPrice(instrument) }
}