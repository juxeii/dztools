package com.jforex.dzjforex.history

import arrow.Kind
import arrow.core.Try
import arrow.core.fix
import arrow.instances.`try`.monadError.monadError
import arrow.typeclasses.MonadError
import com.dukascopy.api.IHistory
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.settings.SettingsDependencies
import com.jforex.kforexutils.history.latestQuote
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

interface HistoryDependencies<F> : SettingsDependencies, MonadError<F, Throwable>
{
    val history: IHistory

    companion object
    {
        operator fun <F> invoke(
            history: IHistory,
            settingsDependencies: SettingsDependencies,
            ME: MonadError<F, Throwable>
        ): HistoryDependencies<F> =
            object : HistoryDependencies<F>,
                SettingsDependencies by settingsDependencies,
                MonadError<F, Throwable> by ME
            {
                override val history = history
            }
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