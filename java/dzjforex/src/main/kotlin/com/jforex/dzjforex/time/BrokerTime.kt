package com.jforex.dzjforex.time

import arrow.data.Reader
import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.isPluginConnected
import com.jforex.dzjforex.misc.isTradingAllowed
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

fun getServerTime(out_ServerTimeToFill: DoubleArray) =
    ReaderApi
        .monad<PluginEnvironment>()
        .binding {
            if (!isPluginConnected().bind()) CONNECTION_LOST_NEW_LOGIN_REQUIRED
            if (!isTradingAllowed().bind()) CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
            else getWhenConnected(out_ServerTimeToFill).bind()
        }.fix()

internal fun getWhenConnected(out_ServerTimeToFill: DoubleArray): Reader<PluginEnvironment, Int> =
    ReaderApi
        .monad<PluginEnvironment>()
        .binding {
            val serverTime = getServerTimeFromContext().bind()
            val isMarketClosed = isMarketClosed(serverTime).bind()
            if (isMarketClosed) CONNECTION_OK_BUT_MARKET_CLOSED
            else {
                fillServerTimeParam(serverTime, out_ServerTimeToFill)
                CONNECTION_OK
            }
        }.fix()

internal fun getServerTimeFromContext() =
    ReaderApi
        .ask<PluginEnvironment>()
        .map { env ->
            env
                .pluginStrategy
                .context
                .time
        }

internal fun isMarketClosed(serverTime: Long) =
    ReaderApi
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

internal fun fillServerTimeParam(
    serverTime: Long,
    out_ServerTimeToFill: DoubleArray
) {
    val serverTimeInSeconds = serverTime / 1000L
    out_ServerTimeToFill[0] = toDATEFormat(serverTimeInSeconds)
}