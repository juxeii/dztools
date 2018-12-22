package com.jforex.dzjforex.sell

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.contractsToAmount
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.zorro.BROKER_SELL_FAIL
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.close

lateinit var brokerSellApi: BrokerSellDependencies<ForIO>

fun initBrokerSellApi()
{
    brokerSellApi = BrokerSellDependencies(pluginApi, contextApi, IO.monadError())
}

interface BrokerSellDependencies<F> : PluginDependencies,
    ContextDependencies,
    MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerSellDependencies<F> =
            object : BrokerSellDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerSellApi
{
    fun <F> BrokerSellDependencies<F>.create(orderId: Int, contracts: Int): Kind<F, Int> =
        bindingCatch {
            getOrderForId(orderId)
                .map { order -> closeOrder(order, contractsToAmount(contracts)).bind() }
                .fold({ BROKER_SELL_FAIL })
                { orderEvent ->
                    if (orderEvent.type == OrderEventType.CLOSE_OK || orderEvent.type == OrderEventType.PARTIAL_CLOSE_OK)
                        orderEvent.order.zorroId()
                    else BROKER_SELL_FAIL
                }
        }

    fun <F> BrokerSellDependencies<F>.closeOrder(order: IOrder, amount: Double): Kind<F, OrderEvent> =
        catch {
            order
                .close(amount = amount) {}
                .blockingLast()
        }
}