package com.jforex.dzjforex.misc

import arrow.core.*
import arrow.data.*
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.latestTick
import com.jforex.dzjforex.zorro.Quotes
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

internal fun waitForFirstQuote(instrument: Instrument): Reader<PluginConfig, Try<TickQuote>> = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        if (hasQuote(instrument).runId(config))
        {
            logger.debug("Tick for $instrument is available in quoteProvider")
            getQuote(instrument).runId(config).success()
        } else
        {
            logger.debug("Tick for $instrument is not available in quoteProvider!")
            latestQuoteFromHistory(instrument)
                .runId(config)
                .fold({
                    logger.debug("Tick for $instrument is not available in history!")
                    Failure(it)
                }, {
                    logger.debug("Tick for $instrument is available in history")
                    it.success()
                })
        }
    }

private fun latestQuoteFromHistory(instrument: Instrument): Reader<PluginConfig, Try<TickQuote>> = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        Observable.interval(
            0,
            config.pluginSettings.historyAccessRetryDelay(),
            TimeUnit.MILLISECONDS
        )
            .take(config.pluginSettings.historyAccessRetries())
            .map { latestTick(instrument).runId(config) }
            .takeUntil { it.isSuccess() }
            .blockingFirst()
    }

internal fun saveQuote(quote: TickQuote) = State<Quotes, Unit> {
    val res = it.plus(Pair(quote.instrument, quote)) toT Unit
    res
}

internal fun <R> getTick(
    instrument: Instrument,
    block: ITick.() -> R
) = ReaderApi
    .ask<PluginConfig>()
    .map { env -> env.quotes[instrument]!!.tick.run(block) }

internal fun getQuote(instrument: Instrument): Reader<PluginConfig, TickQuote> = ReaderApi
    .ask<PluginConfig>()
    .map { env -> env.quotes[instrument]!! }

internal fun <R> getQuotes(block: Quotes.() -> R) = ReaderApi
    .ask<PluginConfig>()
    .map { env -> env.quotes.run(block) }

internal fun hasQuote(instrument: Instrument) = getQuotes { containsKey(instrument) }

internal fun noOfInstruments() = getQuotes { size }

internal fun quotesInstruments() = getQuotes { keys }

internal fun getAsk(instrument: Instrument) = getTick(instrument) { ask }

internal fun getBid(instrument: Instrument) = getTick(instrument) { bid }

internal fun getSpread(instrument: Instrument) = getTick(instrument) { bid - ask }