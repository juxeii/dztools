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
import com.jforex.dzjforex.misc.*
import com.jforex.kforexutils.history.latestQuote
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

lateinit var historyApi: ContextDependencies<ForIO>

fun initHistoryApi()
{
    historyApi = createContextApi(contextApi.context, IO.monadError())
}

object HistoryApi
{
    fun <F> ContextDependencies<F>.waitForLatestQuote(instrument: Instrument): Kind<F, TickQuote>
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