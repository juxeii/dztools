package com.jforex.dzjforex.stop

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.order.OrderRepositoryApi.getOrderForId
import com.jforex.dzjforex.zorro.ADJUST_SL_FAIL
import com.jforex.dzjforex.zorro.ADJUST_SL_OK
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.setSL

lateinit var brokerStopApi: BrokerStopDependencies<ForIO>

fun initBrokerStopApi()
{
    brokerStopApi = BrokerStopDependencies(pluginApi, contextApi, IO.monadError())
}

interface BrokerStopDependencies<F> : PluginDependencies,
    ContextDependencies,
    MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerStopDependencies<F> =
            object : BrokerStopDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerStopApi
{
    fun <F> BrokerStopDependencies<F>.create(orderId: Int, slPrice: Double): Kind<F, Int> =
        bindingCatch {
            getOrderForId(orderId)
                .map { order -> setSLPrice(order, slPrice).bind() }
                .fold({ ADJUST_SL_FAIL })
                { orderEvent ->
                    if (orderEvent.type == OrderEventType.CHANGED_SL) ADJUST_SL_OK else ADJUST_SL_FAIL
                }
        }

    fun <F> BrokerStopDependencies<F>.setSLPrice(order: IOrder, slPrice: Double): Kind<F, OrderEvent> =
        catch {
            order
                .setSL(slPrice = slPrice) {}
                .blockingLast()
        }
}