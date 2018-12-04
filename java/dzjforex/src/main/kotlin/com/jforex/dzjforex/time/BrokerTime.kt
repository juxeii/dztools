package com.jforex.dzjforex.time

import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.isTradingAllowedForAccount
import com.jforex.dzjforex.misc.PluginConfigExt
import com.jforex.dzjforex.misc.getContext
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getServerTime() = getContext { time }

internal fun isMarketClosed(serverTime: Long) = getContext { dataService.isOfflineTime(serverTime) }

internal fun areTradeOrdersAllowed() = ReaderApi
    .monad<PluginConfigExt>()
    .binding {
        if (!isTradingAllowedForAccount().bind()) false
        else noOfTradeableInstruments().bind() > 0
    }.fix()

private fun noOfTradeableInstruments() = ReaderApi
    .ask<PluginConfigExt>()
    .map { config ->
        config
            .kForexUtils
            .context
            .subscribedInstruments
            .stream()
            .filter { config.kForexUtils.context.engine.isTradable(it) }
            .mapToInt { 1 }
            .sum()
    }

internal fun getConnectionState(serverTime: Long) = ReaderApi
    .monad<PluginConfigExt>()
    .binding {
        when {
            isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
            !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
            else -> CONNECTION_OK
        }
    }.fix()
