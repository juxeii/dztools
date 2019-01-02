package com.jforex.dzjforex.history

import arrow.Kind
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.kforexutils.history.latestQuote
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object HistoryApi
{
    fun <F> ContextDependencies<F>.fetchLatestQuote(instrument: Instrument): Kind<F, TickQuote> =
        Observable
            .interval(0, pluginSettings.historyAccessRetryDelay(), TimeUnit.MILLISECONDS)
            .take(pluginSettings.historyAccessRetries())
            .map { history.latestQuote(instrument) }
            .takeUntil { it.isSuccess() }
            .map { tryQuote-> tryQuote.fromTry { it } }
            .blockingFirst()
}