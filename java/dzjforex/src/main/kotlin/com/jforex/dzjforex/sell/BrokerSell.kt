package com.jforex.dzjforex.sell

import arrow.Kind
import arrow.typeclasses.binding
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.command.getBcSlippage
import com.jforex.dzjforex.command.maybeBcLimitPrice
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.contractsToAmount
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
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
    private data class CloseParams(
        val order: IOrder,
        val amount: Double,
        val price: Double,
        val slippage: Double
    )

    fun <F> ContextDependencies<F>.brokerSell(orderId: Int, contracts: Int): Kind<F, Int> =
        binding{
            val order = getOrderForId(orderId).bind()
            if(order.isClosed) {
                logger.warn("BrokerSell Trying to close already closed order! $order state ${order.state}")
                order.zorroId()
            }
            else{
                logger.debug("BrokerSell called orderId $orderId contracts $contracts order $order")
                val closeParams=createCloseParams(order, contracts).bind()
                logger.debug("BrokerSell closeParams $closeParams")
                val orderEvent=closeOrder(closeParams).bind()
                logger.debug("BrokerSell orderEvent $orderEvent")
                processOrderEventAndGetResult(orderEvent)
            }
        } .handleError { error ->
            logger.error("BrokerSell failed! Error: ${error.message} Stack trace: ${getStackTrace(error)}")
            BROKER_SELL_FAIL
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

    private fun <F> ContextDependencies<F>.createCloseParams(order: IOrder, contracts: Int): Kind<F, CloseParams> =
        invoke {
            CloseParams(
                order = order,
                amount = contractsToAmount(contracts),
                price = getLimitPrice(order.instrument),
                slippage = getBcSlippage()
            )
        }

    private fun processOrderEventAndGetResult(orderEvent: OrderEvent): Int =
        if (orderEvent.type == OrderEventType.CLOSE_OK || orderEvent.type == OrderEventType.PARTIAL_CLOSE_OK)
            orderEvent.order.zorroId()
        else BROKER_SELL_FAIL

    private fun getLimitPrice(instrument: Instrument) =
        maybeBcLimitPrice()
            .fold({ TradingSettings.noPreferredClosePrice })
            { limitPrice -> limitPrice.asPrice(instrument) }
}