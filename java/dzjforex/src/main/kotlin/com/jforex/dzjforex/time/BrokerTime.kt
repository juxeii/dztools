package com.jforex.dzjforex.time

import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountApi.isTradingAllowed
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED

object BrokerTimeApi
{
    fun <F> ContextDependencies<F>.brokerTime(out_ServerTimeToFill: DoubleArray) = binding {
        if (!isConnected().bind()) CONNECTION_LOST_NEW_LOGIN_REQUIRED
        else
        {
            val serverTime = jfContext.time
            fillServerTime(out_ServerTimeToFill, serverTime.toUTCTime()).bind()
            getConnectionState(serverTime).bind()
        }
    }

    fun <F> ContextDependencies<F>.getConnectionState(serverTime: Long) = binding {
        when
        {
            isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
            !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
            else -> CONNECTION_OK
        }
    }

    fun <F> ContextDependencies<F>.isMarketClosed(serverTime: Long) = delay {
        jfContext
            .dataService
            .isOfflineTime(serverTime)
    }

    fun <F> ContextDependencies<F>.areTradeOrdersAllowed() = binding {
        if (!isTradingAllowed().bind()) false
        else hasTradeableInstrument().bind()
    }

    fun <F> ContextDependencies<F>.hasTradeableInstrument() =
        getSubscribedInstruments().flatMap { instruments -> isAnyInstrumentTradeable(instruments) }

    fun <F> ContextDependencies<F>.isAnyInstrumentTradeable(instruments: Set<Instrument>) = delay {
        instruments.any { jfContext.engine.isTradable(it) || it.isTradable }
    }

    fun <F> ContextDependencies<F>.fillServerTime(out_ServerTimeToFill: DoubleArray, serverTimeUTC: Double) =
        delay {
            val iTimeUTC = 0
            out_ServerTimeToFill[iTimeUTC] = serverTimeUTC
        }
}