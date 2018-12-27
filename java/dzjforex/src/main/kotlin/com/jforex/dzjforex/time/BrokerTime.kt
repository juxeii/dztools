package com.jforex.dzjforex.time

import arrow.Kind
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.AccountApi.isTradingAllowed
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import com.jforex.dzjforex.misc.PluginApi.isConnected

object BrokerTimeApi
{
    fun <F> ContextDependencies<F>.brokerTime(out_ServerTimeToFill: DoubleArray): Kind<F, Int> =
        binding {
            if (!isConnected()) CONNECTION_LOST_NEW_LOGIN_REQUIRED

            val serverTime = getServerTime().bind()
            val serverTimeInDateFormat = toDATEFormat(serverTime)
            out_ServerTimeToFill[0] = serverTimeInDateFormat
            getConnectionState(serverTime).bind()
        }

    fun <F> ContextDependencies<F>.getServerTime(): Kind<F, Long> = just(context.time)

    fun <F> ContextDependencies<F>.isMarketClosed(serverTime: Long): Kind<F, Boolean> =
        just(context.dataService.isOfflineTime(serverTime))

    fun <F> ContextDependencies<F>.areTradeOrdersAllowed(): Kind<F, Boolean> =
        binding {
            if (!isTradingAllowed()) false
            else hasTradeableInstrument().bind()
        }

    fun <F> ContextDependencies<F>.hasTradeableInstrument(): Kind<F, Boolean> =
        just(context
            .subscribedInstruments
            .stream()
            .anyMatch { context.engine.isTradable(it) })

    fun <F> ContextDependencies<F>.getConnectionState(serverTime: Long): Kind<F, Int> =
        binding {
            when
            {
                isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
                !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
                else -> CONNECTION_OK
            }
        }
}