package com.jforex.dzjforex.misc

import arrow.effects.ForIO
import com.dukascopy.api.*
import com.jforex.dzjforex.zorro.lotScale

lateinit var contextApi: ContextDependencies<ForIO>

fun initContextApi(context: IContext) {
    contextApi = ContextDependencies(context, pluginApi)
}

interface ContextDependencies<F> : PluginDependencies<F> {
    val jfContext: IContext
    val engine: IEngine
    val account: IAccount
    val history: IHistory

    fun Int.toAmount() = Math.abs(this) / lotScale

    fun Double.toContracts() = (this * lotScale).toInt()

    fun Double.toSignedContracts(command: IEngine.OrderCommand) =
        if (command == IEngine.OrderCommand.BUY) toContracts() else -toContracts()

    fun IOrder.toSignedContracts() = amount.toSignedContracts(orderCommand)

    fun IOrder.zorroId() = id.toInt()

    companion object {
        operator fun <F> invoke(
            context: IContext,
            pluginDependencies: PluginDependencies<F>
        ): ContextDependencies<F> =
            object : ContextDependencies<F>, PluginDependencies<F> by pluginDependencies {
                override val jfContext = context
                override val engine = context.engine
                override val account = context.account
                override val history = context.history
            }
    }
}

object ContextApi {
    fun <F> ContextDependencies<F>.getSubscribedInstruments() = delay { jfContext.subscribedInstruments }

    fun <F> ContextDependencies<F>.setSubscribedInstruments(instrumentsToSubscribe: Set<Instrument>) =
        getSubscribedInstruments().map { subscribedInstruments ->
            jfContext.setSubscribedInstruments(subscribedInstruments + instrumentsToSubscribe, false)
        }
}