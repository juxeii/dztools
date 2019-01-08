package com.jforex.dzjforex.time

import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountApi.isTradingAllowed
import com.jforex.dzjforex.buy.BrokerBuyApi.logError
import com.jforex.dzjforex.buy.BrokerBuyData
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.zorro.*

object BrokerTimeApi
{
    fun <F> ContextDependencies<F>.brokerTime() = binding {
        if (!isConnected().bind()) BrokerTimeData(CONNECTION_LOST_NEW_LOGIN_REQUIRED)
        else
        {
            val serverTime = getServerTime().bind()
            BrokerTimeData(getConnectionState(serverTime).bind(), serverTime.toUTCTime())
        }
    }

    fun <F> ContextDependencies<F>.getServerTime() = delay { jfContext.time }

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
}