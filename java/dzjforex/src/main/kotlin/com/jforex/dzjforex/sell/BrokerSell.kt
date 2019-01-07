package com.jforex.dzjforex.sell

import arrow.Kind
import arrow.core.Option
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.OrderLookupApi.getTradeableOrderForId
import com.jforex.dzjforex.zorro.BROKER_SELL_FAIL
import com.jforex.kforexutils.misc.asPrice
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.close
import com.jforex.kforexutils.order.extension.isClosed
import com.jforex.kforexutils.settings.TradingSettings

object BrokerSellApi {

    data class CloseParams(val order: IOrder, val amount: Double, val price: Double, val slippage: Double)

    fun <F> ContextDependencies<F>.brokerSell(
        orderId: Int,
        contracts: Int,
        maybeLimitPrice: Option<Double>,
        slippage: Double
    ) =
        getTradeableOrderForId(orderId)
            .flatMap { order ->
                if (order.isClosed) {
                    logger.warn("BrokerSell: trying to close already closed order $order!")
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
                    logger.debug("BrokerSell: closing $closeParams")
                    closeOrder(closeParams).map { orderEvent -> processOrderEventAndGetResult(orderEvent) }
                }
            }.handleError { error ->
                logError(error)
                BROKER_SELL_FAIL
            }

    fun <F> ContextDependencies<F>.closeOrder(closeParams: CloseParams): Kind<F, OrderEvent> =
        delay {
            closeParams
                .order
                .close(
                    amount = closeParams.amount,
                    price = closeParams.price,
                    slippage = closeParams.slippage
                ) {}
                .blockingLast()
        }

    fun processOrderEventAndGetResult(orderEvent: OrderEvent) =
        if (orderEvent.type == OrderEventType.CLOSE_OK || orderEvent.type == OrderEventType.PARTIAL_CLOSE_OK)
            orderEvent.order.zorroId()
        else BROKER_SELL_FAIL

    fun <F> ContextDependencies<F>.logError(error: Throwable) = delay {
        when (error) {
            is OrderIdNotFoundException -> {
                logAndPrintErrorOnZorro("BrokerSell: orderId ${error.orderId} not found!")
            }
            is AssetNotTradeableException -> {
                logAndPrintErrorOnZorro("BrokerSell: asset ${error.instrument} currently not tradeable!")
            }
            else -> {
                logger.error("BrokerSell failed! Error: ${error.message} Stack trace: ${getStackTrace(error)}")
            }
        }
    }

    fun getLimitPrice(maybeLimitPrice: Option<Double>, instrument: Instrument) =
        maybeLimitPrice
            .fold({ TradingSettings.noPreferredClosePrice })
            { limitPrice -> limitPrice.asPrice(instrument) }
}