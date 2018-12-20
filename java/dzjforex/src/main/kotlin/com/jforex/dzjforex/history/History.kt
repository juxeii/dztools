package com.jforex.dzjforex.history

import arrow.Kind
import arrow.core.ForTry
import arrow.core.Try
import arrow.core.fix
import arrow.instances.`try`.monadError.monadError
import arrow.typeclasses.MonadError
import com.dukascopy.api.IContext
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.time.initBrokerTimeApi
import com.jforex.kforexutils.history.latestQuote
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

lateinit var historyApi: HistoryDependencies<ForTry>

fun initHistoryApi()
{
    historyApi = HistoryDependencies(pluginApi, contextApi, Try.monadError())
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
                .map { history.latestQuote(instrument, Try.monadError()).fix() }
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