package com.jforex.dzjforex.sell

import arrow.core.Option
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

    fun <F> ContextDependencies<F>.brokerSell(
        orderId: Int,
        contracts: Int,
        maybeLimitPrice: Option<Double>,
        slippage: Double
    ) =
        getTradeableOrderForId(orderId)
            .map { order ->
                if (order.isClosed) {
                    logger.warn("BrokerSell: trying to close already closed order $order!")
                    order.zorroId()
                } else
                    order
                        .close(
                            amount = contracts.toAmount(),
                            price = getLimitPrice(maybeLimitPrice, order.instrument),
                            slippage = slippage
                        ) {}
                        .map { orderEvent -> processOrderEventAndGetResult(orderEvent) }
                        .blockingLast()
            }.handleError { error ->
                logError(error)
                BROKER_SELL_FAIL
            }

    private fun <F> ContextDependencies<F>.logError(error: Throwable) = delay {
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

    private fun processOrderEventAndGetResult(orderEvent: OrderEvent) =
        if (orderEvent.type == OrderEventType.CLOSE_OK || orderEvent.type == OrderEventType.PARTIAL_CLOSE_OK)
            orderEvent.order.zorroId()
        else BROKER_SELL_FAIL

    private fun getLimitPrice(maybeLimitPrice: Option<Double>, instrument: Instrument) =
        maybeLimitPrice
            .fold({ TradingSettings.noPreferredClosePrice })
            { limitPrice -> limitPrice.asPrice(instrument) }
}