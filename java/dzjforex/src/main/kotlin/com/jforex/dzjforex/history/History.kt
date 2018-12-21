package com.jforex.dzjforex.history

import arrow.Kind
import arrow.core.ForTry
import arrow.core.Try
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.instances.`try`.monadError.monadError
import arrow.typeclasses.MonadError
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.kforexutils.history.latestQuote
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

lateinit var historyApi: HistoryDependencies<ForIO>

fun initHistoryApi()
{
    historyApi = HistoryDependencies(pluginApi, contextApi, IO.monadError())
}

interface HistoryDependencies<F> : PluginDependencies, ContextDependencies, MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            ME: MonadError<F, Throwable>
        ): HistoryDependencies<F> =
            object : HistoryDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object HistoryApi
{
    fun <F> HistoryDependencies<F>.waitForLatestQuote(instrument: Instrument): Kind<F, TickQuote>
    {
        return catch {
            Observable.interval(
                0,
                pluginSettings.historyAccessRetryDelay(),
                TimeUnit.MILLISECONDS
            )
                .take(pluginSettings.historyAccessRetries())
                .map { history.latestQuote(instrument) }
                .takeUntil { it.isSuccess() }
                .map {
                    it.fold({ error ->
                        throw(JFException("Latest tick qoute not available for $instrument! $error"))
                    })
                    { tickQuote -> tickQuote }
                }
                .blockingFirst()
        }
    }
}