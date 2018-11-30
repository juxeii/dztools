package com.jforex.dzjforex.misc

import arrow.core.left
import arrow.core.right
import arrow.data.ReaderApi
import arrow.data.ReaderT
import arrow.data.map
import arrow.data.runId
import arrow.instances.either.monad.map
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.IHistoryError
import com.jforex.dzjforex.history.latestTick
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

internal fun waitForFirstQuote(instrument: Instrument) = ReaderT { env: PluginEnvironment ->
    val quoteProvider = env.pluginStrategy.quoteProvider

    historyRetryObservable()
        .runId(env)
        .takeWhile {
            logger.debug("Trying to fetch latest tick for $instrument, try no: $it")
            if (quoteProvider.hasTick(instrument)) {
                logger.debug("Tick for $instrument is available in quoteProvider")
                false
            } else {
                logger.debug("Tick for $instrument is not available in quoteProvider!")
                latestTick(instrument)
                    .run(env)
                    .map {
                        logger.debug("Tick for $instrument is available in history")
                        quoteProvider.store(instrument, it)
                    }
                    .fold({
                        logger.debug("Tick for $instrument is not available in history!")
                        true
                    }, { false })
            }
        }.blockingSubscribe()

    if (quoteProvider.hasTick(instrument)) Unit.right()
    else IHistoryError.IHistoryException.left()
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


class QuoteProvider(private val kForexUtils: KForexUtils) {
    private var latestTicks: MutableMap<Instrument, TickQuote> = mutableMapOf()
    private val logger = LogManager.getLogger(QuoteProvider::class.java)

    init {
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = {
                logger.debug("Received tick quote $it")
                latestTicks[it.instrument] = it
            })
    }

    private fun <R> tick(
        instrument: Instrument,
        block: ITick.() -> R
    ) = latestTicks[instrument]!!.tick.run(block)

    fun hasTick(instrument: Instrument) = latestTicks.containsKey(instrument)

    fun store(
        instrument: Instrument,
        quote: TickQuote
    ) = latestTicks.putIfAbsent(instrument, quote)

    fun ask(instrument: Instrument): Double = tick(instrument) { ask }

    fun bid(instrument: Instrument) = tick(instrument) { bid }

    fun spread(instrument: Instrument) = tick(instrument) { bid - ask }
}