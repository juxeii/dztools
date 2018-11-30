package com.jforex.dzjforex.history

import arrow.core.Try
import arrow.core.left
import arrow.core.right
import arrow.data.ReaderApi
import arrow.data.ReaderT
import arrow.data.map
import com.dukascopy.api.IHistory
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.kforexutils.price.TickQuote
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal sealed class IHistoryError {
    object IHistoryException : IHistoryError()
    object IHistoryNPE : IHistoryError()
}

internal fun <R> historyInfo(block: IHistory.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .history
            .run(block)
    }

internal fun latestTick(instrument: Instrument) = ReaderT { env: PluginEnvironment ->
    Try { env.pluginStrategy.history.getLastTick(instrument) }
        .fold({
            logger.debug("Latest tick from history for $instrument throwed exception $it")
            IHistoryError.IHistoryException.left()
        },
            { tick ->
                if (tick == null) {
                    logger.debug("Latest tick from history for $instrument returned null!")
                    IHistoryError.IHistoryNPE.left()
                } else TickQuote(instrument, tick).right()
            })
}