package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import com.dukascopy.api.*

lateinit var contextApi: ContextDependencies<ForIO>

fun initContextApi(context: IContext)
{
    contextApi = ContextDependencies(context, pluginApi, IO.monadError())
}

fun <F> createContextApi(context: IContext, ME: MonadError<F, Throwable>) = ContextDependencies(context, pluginApi, ME)

fun <F> createQuoteApi(context: IContext, ME: MonadError<F, Throwable>) =
    QuoteDependencies(createContextApi(context, ME), createQuoteProviderApi())

interface ContextDependencies<F> : PluginDependencies, MonadError<F, Throwable>
{
    val context: IContext
    val engine: IEngine
    val account: IAccount
    val history: IHistory

    companion object
    {
        operator fun <F> invoke(
            context: IContext,
            pluginDependencies: PluginDependencies,
            ME: MonadError<F, Throwable>
        ): ContextDependencies<F> =
            object : ContextDependencies<F>,
                PluginDependencies by pluginDependencies,
                MonadError<F, Throwable> by ME
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
        just(context.subscribedInstruments)
}