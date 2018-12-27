package com.jforex.dzjforex.history

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.createContextApi
import com.jforex.kforexutils.history.latestQuote
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object HistoryApi
{
    fun <F> ContextDependencies<F>.waitForLatestQuote(instrument: Instrument): Kind<F, TickQuote> =
        bindingCatch {
            getRetryInterval()
                .bind()
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

    private fun <F> ContextDependencies<F>.getRetryInterval(): Kind<F, Observable<Long>> =
        just(
            Observable.interval(
                0,
                pluginSettings.historyAccessRetryDelay(),
                TimeUnit.MILLISECONDS
            ).take(pluginSettings.historyAccessRetries())
        )
}