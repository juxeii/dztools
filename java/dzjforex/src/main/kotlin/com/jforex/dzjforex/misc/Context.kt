package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadDefer.monadDefer
import arrow.effects.instances.io.monadError.monadError
import arrow.effects.typeclasses.MonadDefer
import arrow.typeclasses.MonadError
import com.dukascopy.api.*

lateinit var contextApi: ContextDependencies<ForIO>

fun initContextApi(context: IContext)
{
    contextApi = ContextDependencies(context, pluginApi, IO.monadDefer())
}

fun <F> createContextApi(context: IContext, MD: MonadDefer<F>) = ContextDependencies(context, pluginApi, MD)

fun <F> createQuoteApi(context: IContext, MD: MonadDefer<F>) =
    QuoteDependencies(createContextApi(context, MD), createQuoteProviderApi())

interface ContextDependencies<F> : PluginDependencies, MonadDefer<F>
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
            MD: MonadDefer<F>
        ): ContextDependencies<F> =
            object : ContextDependencies<F>,
                PluginDependencies by pluginDependencies,
                MonadDefer<F> by MD
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