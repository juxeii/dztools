package com.jforex.dzjforex.time

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.isTradingAllowedForAccount
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.isPluginConnected
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal data class BrokerTimeResult(
    val callResult: Int,
    val maybeTime: Option<Double>
)

internal fun getServerTime() = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (!isPluginConnected().bind()) createBrokerTimeResult(CONNECTION_LOST_NEW_LOGIN_REQUIRED)
        else if (!areTradeOrdersAllowed().bind()) createBrokerTimeResult(CONNECTION_OK_BUT_TRADING_NOT_ALLOWED)
        else getWhenConnected().bind()
    }.fix()

private fun getWhenConnected() = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        val serverTime = getServerTimeFromContext().bind()
        val isMarketClosed = isMarketClosed(serverTime).bind()
        if (isMarketClosed) createBrokerTimeResult(CONNECTION_OK_BUT_MARKET_CLOSED)
        else createBrokerTimeResult(CONNECTION_OK, Some(toDATEFormatInSeconds(serverTime)))
    }.fix()

private fun getServerTimeFromContext() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .context
            .time
    }

private fun isMarketClosed(serverTime: Long) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        val isClosed = env
            .pluginStrategy
            .context
            .dataService
            .isOfflineTime(serverTime)
        logger.debug("time is $serverTime market is closed $isClosed")
        isClosed
    }

private fun createBrokerTimeResult(
    callResult: Int,
    maybeTime: Option<Double> = None
) = BrokerTimeResult(callResult, maybeTime)

private fun noOfTradeableInstruments() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .client
            .subscribedInstruments
            .stream()
            .filter { it.isTradable }
            .mapToInt { 1 }
            .sum()
    }

private fun areTradeOrdersAllowed() = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (!isTradingAllowedForAccount().bind()) false
        else noOfTradeableInstruments().bind() > 0
    }.fix()