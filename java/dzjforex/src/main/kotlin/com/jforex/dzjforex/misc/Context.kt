package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.effects.ForIO
import com.dukascopy.api.*

lateinit var contextApi: ContextDependencies<ForIO>

fun initContextApi(context: IContext)
{
    contextApi = ContextDependencies(context, pluginApi)
}

fun createContextApi(context: IContext) = ContextDependencies(context, pluginApi)

fun createQuoteApi(context: IContext) =
    QuoteDependencies(createContextApi(context), createQuoteProviderApi())

interface ContextDependencies<F> : PluginDependencies<F>
{
    val context: IContext
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
                override val context = context
                override val engine = context.engine
                override val account = context.account
                override val history = context.history
            }
    }
}

interface QuoteDependencies<F> : ContextDependencies<F>, QuoteProviderDependencies
{
    companion object
    {
        operator fun <F> invoke(
            contextDependencies: ContextDependencies<F>,
            quoteProviderDependencies: QuoteProviderDependencies
        ): QuoteDependencies<F> =
            object : QuoteDependencies<F>,
                ContextDependencies<F> by contextDependencies,
                QuoteProviderDependencies by quoteProviderDependencies
            {}
    }
}

object ContextApi
{
    fun <F> ContextDependencies<F>.getSubscribedInstruments(): Kind<F, Set<Instrument>> =
        invoke { context.subscribedInstruments }
}