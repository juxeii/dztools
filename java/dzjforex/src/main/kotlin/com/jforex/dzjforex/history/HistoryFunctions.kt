package com.jforex.dzjforex.history

import arrow.core.Try
import arrow.data.ReaderApi
import arrow.data.map
import arrow.data.runId
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.PluginConfig
import com.jforex.dzjforex.misc.PluginConfigExt
import com.jforex.dzjforex.misc.getHistory
import com.jforex.kforexutils.price.TickQuote
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun latestTick(instrument: Instrument) = ReaderApi
    .ask<PluginConfigExt>()
    .map { config ->
        Try {
            val tick = getHistory { getLastTick(instrument) }.runId(config)
            if (tick == null)
            {
                logger.debug("Latest tick from history for $instrument returned null!")
                throw JFException("Latest tick from history for $instrument returned null!")
            } else TickQuote(instrument, tick)
        }
    }