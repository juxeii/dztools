package com.jforex.dzjforex.history

import arrow.core.Try
import arrow.data.ReaderApi
import arrow.data.map
import com.dukascopy.api.IHistory
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.kforexutils.price.TickQuote
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun <R> historyInfo(block: IHistory.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .history
            .run(block)
    }

internal fun latestTick(instrument: Instrument) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        Try {
            val tick = env.pluginStrategy.history.getLastTick(instrument)
            if (tick == null)
            {
                logger.debug("Latest tick from history for $instrument returned null!")
                throw JFException("Latest tick from history for $instrument returned null!")
            } else TickQuote(instrument, tick)
        }
    }