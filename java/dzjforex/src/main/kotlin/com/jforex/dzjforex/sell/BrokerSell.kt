package com.jforex.dzjforex.sell

import arrow.Kind
import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.command.getBcSlippage
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.contractsToAmount
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.zorro.BROKER_SELL_FAIL
import com.jforex.kforexutils.misc.asPrice
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.close
import com.jforex.kforexutils.settings.TradingSettings

object BrokerSellApi
{
    private val bcLimitPrice: BehaviorRelay<Option<Double>> = BehaviorRelay.createDefault(None)

    private data class CloseParams(val price: Double, val slippage: Double)

    fun <F> ContextDependencies<F>.brokerSell(orderId: Int, contracts: Int): Kind<F, Int> =
        bindingCatch {
            val order = getOrderForId(orderId).bind()
            val orderEvent = closeOrder(order, createCloseParams(order.instrument), contracts).bind()
            processCloseResult(orderEvent)
        }.handleError {
            logger.debug("BrokerSell failed! ${getStackTrace(it)}")
            BROKER_SELL_FAIL
        }

    private fun <F> ContextDependencies<F>.closeOrder(
        order: IOrder,
        closeParams: CloseParams,
        contracts: Int
    ): Kind<F, OrderEvent> = just(order
        .close(
            amount = contractsToAmount(contracts),
            price = closeParams.price,
            slippage = closeParams.slippage
        ) {}
        .blockingLast())

    private fun createCloseParams(instrument: Instrument) =
        maybeLimitPrice().fold(
            {
                CloseParams(
                    price = TradingSettings.noPreferredClosePrice,
                    slippage = getBcSlippage()
                )
            })
        { limitPrice ->
            CloseParams(
                price = limitPrice.asPrice(instrument),
                slippage = getBcSlippage()
            )
        }

    private fun processCloseResult(orderEvent: OrderEvent): Int =
        if (orderEvent.type == OrderEventType.CLOSE_OK || orderEvent.type == OrderEventType.PARTIAL_CLOSE_OK)
            orderEvent.order.zorroId()
        else BROKER_SELL_FAIL

    private fun maybeLimitPrice() = bcLimitPrice.value!!

    fun setLmitPrice(limit: Double) = bcLimitPrice.accept(limit.some())
}