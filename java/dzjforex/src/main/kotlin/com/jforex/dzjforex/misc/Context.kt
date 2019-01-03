package com.jforex.dzjforex.misc

import arrow.effects.ForIO
import com.dukascopy.api.*

lateinit var contextApi: ContextDependencies<ForIO>

fun initContextApi(context: IContext)
{
    contextApi = ContextDependencies(context, pluginApi)
}

interface ContextDependencies<F> : PluginDependencies<F>
{
    val jfContext: IContext
    val engine: IEngine
    val account: IAccount
    val history: IHistory

    companion object
    {
        operator fun <F> invoke(
            context: IContext,
            pluginDependencies: PluginDependencies<F>
        ): ContextDependencies<F> =
            object : ContextDependencies<F>,
                PluginDependencies<F> by pluginDependencies
            {
                override val jfContext = context
                override val engine = context.engine
                override val account = context.account
                override val history = context.history
            }
    }
}

object ContextApi
{
    fun <F> ContextDependencies<F>.getSubscribedInstruments() = invoke { jfContext.subscribedInstruments }

    fun <F> ContextDependencies<F>.setSubscribedInstruments(instrumentsToSubscribe: Set<Instrument>) =
        getSubscribedInstruments().map { subscribedInstruments ->
            jfContext.setSubscribedInstruments(subscribedInstruments + instrumentsToSubscribe, false)
        }
}