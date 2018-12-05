package com.jforex.dzjforex.time

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.data.runId
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.isTradingAllowedForAccount
import com.jforex.dzjforex.misc.PluginConfig
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.misc.getContext
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal data class BrokerTimeResult(
    val connectionState: Int,
    val maybeServerTime: Option<Double> = None
)

internal fun getBrokerTimeResult() = ReaderApi
    .monad<PluginConfig>()
    .binding {
        if (!getClient { isConnected }.bind()) BrokerTimeResult(CONNECTION_LOST_NEW_LOGIN_REQUIRED)
        else {
            val serverTime = getServerTime().bind()
            val serverTimeInDateFormat = toDATEFormatInSeconds(serverTime)
            val connectionState = getConnectionState(serverTime).bind()
            BrokerTimeResult(connectionState, serverTimeInDateFormat.some())
        }
    }.fix()

internal fun getServerTime() = getContext {
    logger.debug("Server time is $time")
    time
}

internal fun isMarketClosed(serverTime: Long) = getContext { dataService.isOfflineTime(serverTime) }

internal fun areTradeOrdersAllowed() = ReaderApi
    .monad<PluginConfig>()
    .binding {
        if (!isTradingAllowedForAccount().bind()) false
        else noOfTradeableInstruments().bind() > 0
    }.fix()

internal fun noOfTradeableInstruments() = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        getContext {
            subscribedInstruments
                .stream()
                .filter { getContext { engine.isTradable(it) }.runId(config) }
                .mapToInt { 1 }
                .sum()
        }.runId(config)
    }

internal fun getConnectionState(serverTime: Long) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        when {
            isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
            !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
            else -> CONNECTION_OK
        }
    }.fix()
