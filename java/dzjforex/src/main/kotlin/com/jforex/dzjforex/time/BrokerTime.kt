package com.jforex.dzjforex.time

import arrow.Kind
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.AccountApi.isTradingAllowed
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InstrumentApi.isTradeable
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED

sealed class BrokerTimeResult(val returnCode: Int)
{
    data class Failure(val code: Int) : BrokerTimeResult(code)
    data class Success(val code: Int, val serverTime: Double) : BrokerTimeResult(code)
}
typealias BrokerTimeFailure = BrokerTimeResult.Failure
typealias BrokerTimeSuccess = BrokerTimeResult.Success

object BrokerTimeApi
{
    fun <F> ContextDependencies<F>.brokerTime(): Kind<F, BrokerTimeResult> = binding {
        if (!isConnected().bind()) BrokerTimeFailure(CONNECTION_LOST_NEW_LOGIN_REQUIRED)
        else
        {
            val serverTime = getServerTime().bind()
            BrokerTimeSuccess(getConnectionState(serverTime).bind(), serverTime.toUTCTime())
        }
    }

    fun <F> ContextDependencies<F>.getServerTime(): Kind<F, Long> = invoke { context.time }

    fun <F> ContextDependencies<F>.getConnectionState(serverTime: Long): Kind<F, Int> =
        binding {
            when
            {
                isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
                !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
                else -> CONNECTION_OK
            }
        }

    fun <F> ContextDependencies<F>.isMarketClosed(serverTime: Long): Kind<F, Boolean> =
        invoke {
            context
                .dataService
                .isOfflineTime(serverTime)
        }

    fun <F> ContextDependencies<F>.areTradeOrdersAllowed(): Kind<F, Boolean> =
        binding {
            if (!isTradingAllowed()) false
            else hasTradeableInstrument().bind()
        }

    fun <F> ContextDependencies<F>.hasTradeableInstrument(): Kind<F, Boolean> =
        binding {
            getSubscribedInstruments()
                .bind()
                .any { instrument -> isTradeable(instrument).bind() }
        }
}