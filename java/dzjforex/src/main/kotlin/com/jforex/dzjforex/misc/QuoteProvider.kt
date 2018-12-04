package com.jforex.dzjforex.misc

import arrow.core.failure
import arrow.core.success
import arrow.core.toT
import arrow.data.ReaderApi
import arrow.data.State
import arrow.data.map
import arrow.data.runId
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.history.latestTick
import com.jforex.dzjforex.zorro.Quotes
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

internal fun waitForFirstQuote(instrument: Instrument) = ReaderApi
    .ask<PluginConfigExt>()
    .map { config ->
        historyRetryObservable()
            .runId(config)
            .doOnNext { logger.debug("Fetch trigger no $it") }
            .takeWhile {
                logger.debug("Trying to fetch latest tick for $instrument, try no: $it")
                if (hasTick(instrument).runId(config)) {
                    logger.debug("Tick for $instrument is available in quoteProvider")
                    false
                } else {
                    logger.debug("Tick for $instrument is not available in quoteProvider!")
                    latestTick(instrument)
                        .runId(config)
                        .map { quote ->
                            logger.debug("Tick for $instrument is available in history")
                            config.infoStrategy.onTick(instrument, quote.tick)
                        }
                        .fold({
                            logger.debug("Tick for $instrument is not available in history!")
                            true
                        }, { false })
                }
            }.blockingSubscribe()

        logger.debug("Done Fetching")
        if (hasTick(instrument).runId(config)) Unit.success()
        else JFException("Latest tick from history for $instrument returned null!").failure()
    }

private fun historyRetryObservable() = ReaderApi
    .ask<PluginConfigExt>()
    .map { env ->
        Observable
            .interval(
                0,
                env.pluginConfig.pluginSettings.historyAccessRetryDelay(),
                TimeUnit.MILLISECONDS
            ).take(env.pluginConfig.pluginSettings.historyAccessRetries())
    }

internal fun saveQuote(quote: TickQuote) = State<Quotes, Unit> {
    val res = it.plus(Pair(quote.instrument, quote)) toT Unit
    res
}

internal fun <R> getTick(
    instrument: Instrument,
    block: ITick.() -> R
) = ReaderApi
    .ask<PluginConfigExt>()
    .map { env -> env.quotes[instrument]!!.tick.run(block) }

internal fun <R> getQuotes(block: Quotes.() -> R) = ReaderApi
    .ask<PluginConfigExt>()
    .map { env -> env.quotes.run(block) }

internal fun hasTick(instrument: Instrument) = getQuotes { containsKey(instrument) }

internal fun noOfInstruments() = getQuotes { size }

internal fun quotesInstruments() = getQuotes { keys }

internal fun getAsk(instrument: Instrument) = getTick(instrument) { ask }

internal fun getBid(instrument: Instrument) = getTick(instrument) { bid }

internal fun getSpread(instrument: Instrument) = getTick(instrument) { bid - ask }