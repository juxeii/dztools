package com.jforex.dzjforex.time

import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.isTradingAllowedForAccount
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.misc.getContext
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getServerTime(out_ServerTimeToFill: DoubleArray) = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (!getClient { isConnected }.bind()) CONNECTION_LOST_NEW_LOGIN_REQUIRED
        else
        {
            val serverTime = getContext { time }.bind()
            out_ServerTimeToFill[0] = toDATEFormatInSeconds(serverTime)
            when
            {
                isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
                !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
                else -> CONNECTION_OK
            }
        }
    }.fix()

private fun isMarketClosed(serverTime: Long) = getContext { dataService.isOfflineTime(serverTime) }

private fun noOfTradeableInstruments() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .context
            .subscribedInstruments
            .stream()
            .filter { env.pluginStrategy.context.engine.isTradable(it) }
            .mapToInt { 1 }
            .sum()
    }

private fun areTradeOrdersAllowed() = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (!isTradingAllowedForAccount().bind()) false
        else noOfTradeableInstruments().bind() > 0
    }.fix()