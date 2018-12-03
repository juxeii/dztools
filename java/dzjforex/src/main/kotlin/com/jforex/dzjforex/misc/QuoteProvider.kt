package com.jforex.dzjforex.misc

import arrow.core.failure
import arrow.core.success
import arrow.core.toT
import arrow.data.*
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.history.latestTick
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

typealias Quotes = Map<Instrument, TickQuote>

internal fun waitForFirstQuote(instrument: Instrument) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        historyRetryObservable()
            .runId(env)
            .doOnNext { logger.debug("Fetch trigger no $it") }
            .takeWhile {
                logger.debug("Trying to fetch latest tick for $instrument, try no: $it")
                if (hasTick(instrument).runId(env))
                {
                    logger.debug("Tick for $instrument is available in quoteProvider")
                    false
                } else
                {
                    logger.debug("Tick for $instrument is not available in quoteProvider!")
                    latestTick(instrument)
                        .runId(env)
                        .map {
                            logger.debug("Tick for $instrument is available in history")
                            storeQuote(it).runId(env)
                        }
                        .fold({
                            logger.debug("Tick for $instrument is not available in history!")
                            true
                        }, { false })
                }
            }.blockingSubscribe()

        logger.debug("Done Fetching")
        if (hasTick(instrument).runId(env)) Unit.success()
        else JFException("Latest tick from history for $instrument returned null!").failure()
    }

private fun historyRetryObservable() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        Observable
            .interval(
                0,
                env.pluginSettings.historyAccessRetryDelay(),
                TimeUnit.MILLISECONDS
            ).take(env.pluginSettings.historyAccessRetries())
    }

internal fun subscribeQuotes() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .kForexUtils
            .tickQuotes
            .subscribeBy(onNext = {
                env.quotes = saveQuote(it).runS(env.quotes)
            })
    }

private fun saveQuote(quote: TickQuote) = State<Quotes, Unit> {
    val res = it.plus(Pair(quote.instrument, quote)) toT Unit
    res
}

internal fun <R> getTick(
    instrument: Instrument,
    block: ITick.() -> R
) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.quotes[instrument]!!.tick.run(block) }

internal fun <R> getQuotes(block: Quotes.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.quotes.run(block) }

internal fun hasTick(instrument: Instrument) = getQuotes { containsKey(instrument) }

internal fun noOfInstruments() = getQuotes { size }

internal fun quotesInstruments() = getQuotes { keys }

internal fun storeQuote(quote: TickQuote) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.quotes = saveQuote(quote).runS(env.quotes) }

internal fun getAsk(instrument: Instrument) = getTick(instrument) { ask }

internal fun getBid(instrument: Instrument) = getTick(instrument) { bid }

internal fun getSpread(instrument: Instrument) = getTick(instrument) { bid - ask }