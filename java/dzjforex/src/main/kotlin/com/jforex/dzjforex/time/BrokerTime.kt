package com.jforex.dzjforex.time

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monad.monad
import arrow.typeclasses.Monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.AccountDependencies
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import com.jforex.dzjforex.account.AccountApi.isTradingAllowed

lateinit var brokerTimeApi: BrokerTimeDependencies<ForIO>

fun initBrokerTimeApi()
{
    brokerTimeApi = BrokerTimeDependencies(pluginApi, contextApi, IO.monad())
}

interface BrokerTimeDependencies<F> : PluginDependencies,
    ContextDependencies,
    AccountDependencies,
    Monad<F>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            M: Monad<F>
        ): BrokerTimeDependencies<F> =
            object : BrokerTimeDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                Monad<F> by M
            {}
    }
}

object BrokerTimeApi
{
    fun <F> BrokerTimeDependencies<F>.create(out_ServerTimeToFill: DoubleArray): Kind<F, Int> =
        binding {
            if (client.isConnected) CONNECTION_LOST_NEW_LOGIN_REQUIRED

            val serverTime = getServerTime().bind()
            val serverTimeInDateFormat = toDATEFormatInSeconds(serverTime)
            out_ServerTimeToFill[0] = serverTimeInDateFormat
            getConnectionState(serverTime).bind()
        }

    fun <F> BrokerTimeDependencies<F>.getServerTime(): Kind<F, Long> = just(context.time)

    fun <F> BrokerTimeDependencies<F>.isMarketClosed(serverTime: Long): Kind<F, Boolean> =
        just(context.dataService.isOfflineTime(serverTime))

    fun <F> BrokerTimeDependencies<F>.areTradeOrdersAllowed(): Kind<F, Boolean> =
        binding {
            if (!isTradingAllowed()) false
            else hasTradeableInstrument().bind()
        }

    fun <F> BrokerTimeDependencies<F>.hasTradeableInstrument(): Kind<F, Boolean> =
        just(context
            .subscribedInstruments
            .stream()
            .anyMatch { context.engine.isTradable(it) })

    fun <F> BrokerTimeDependencies<F>.getConnectionState(serverTime: Long): Kind<F, Int> =
        binding {
            when
            {
                isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
                !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
                else -> CONNECTION_OK
            }
        }
}