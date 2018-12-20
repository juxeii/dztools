package com.jforex.dzjforex.time

import arrow.Kind
import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.typeclasses.MonadError
import arrow.typeclasses.binding
import com.dukascopy.api.IContext
import com.jforex.dzjforex.account.AccountApi.isTradingAllowedForAccount
import com.jforex.dzjforex.account.AccountDependencies
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

data class BrokerTimeResult(
    val connectionState: Int,
    val serverTime: Double
)

interface BrokerTimeDependencies<F> : MonadError<F, Throwable>, AccountDependencies
{
    val context: IContext

    companion object
    {
        operator fun <F> invoke(
            ME: MonadError<F, Throwable>,
            accountDeps: AccountDependencies,
            context: IContext
        ): BrokerTimeDependencies<F> =
            object : BrokerTimeDependencies<F>,
                MonadError<F, Throwable> by ME,
                AccountDependencies by accountDeps
            {
                override val context = context
            }
    }
}

object BrokerTimeApi
{
    fun <F> BrokerTimeDependencies<F>.getBrokerTimeResult(): Kind<F, BrokerTimeResult> = binding {
        val serverTime = getServerTime().bind()
        val serverTimeInDateFormat = toDATEFormatInSeconds(serverTime)
        val connectionState = getConnectionState(serverTime).bind()
        BrokerTimeResult(connectionState, serverTimeInDateFormat)
    }

    fun <F> BrokerTimeDependencies<F>.getServerTime(): Kind<F, Long> = just(context.time)

    fun <F> BrokerTimeDependencies<F>.isMarketClosed(serverTime: Long): Kind<F, Boolean> =
        just(context.dataService.isOfflineTime(serverTime))

    fun <F> BrokerTimeDependencies<F>.areTradeOrdersAllowed(): Kind<F, Boolean> = binding {
        if (!isTradingAllowedForAccount()) false
        else hasTradeableInstrument().bind()
    }

    fun <F> BrokerTimeDependencies<F>.hasTradeableInstrument(): Kind<F, Boolean> = just(context
        .subscribedInstruments
        .stream()
        .anyMatch { context.engine.isTradable(it) })

    fun <F> BrokerTimeDependencies<F>.getConnectionState(serverTime: Long): Kind<F, Int> = binding {
        when{
            isMarketClosed(serverTime).bind() -> CONNECTION_OK_BUT_MARKET_CLOSED
            !areTradeOrdersAllowed().bind() -> CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
            else -> CONNECTION_OK
        }
    }
}