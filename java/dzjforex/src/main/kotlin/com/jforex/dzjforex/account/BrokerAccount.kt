package com.jforex.dzjforex.account

import arrow.Kind
import com.jforex.dzjforex.account.AccountApi.baseEequity
import com.jforex.dzjforex.account.AccountApi.tradeVal
import com.jforex.dzjforex.account.AccountApi.usedMargin
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE

object BrokerAccountApi
{
    data class BrokerAccountData(val balance: Double, val tradeVal: Double, val marginVal: Double)

    fun <F> ContextDependencies<F>.brokerAccount(out_AccountInfoToFill: DoubleArray): Kind<F, Int> =
        getAccountData()
            .flatMap { accountData -> fillAccountData(out_AccountInfoToFill, accountData) }
            .map { ACCOUNT_AVAILABLE }
            .handleError { error ->
                logger.error(
                    "BrokerAccount failed! Error message: ${error.message} " +
                            "Stack trace: ${getStackTrace(error)}"
                )
                ACCOUNT_UNAVAILABLE
            }

    fun <F> ContextDependencies<F>.getAccountData(): Kind<F, BrokerAccountData> =
        map(baseEequity(), tradeVal(), usedMargin())
        { BrokerAccountData(balance = it.a, tradeVal = it.b, marginVal = it.c) }

    fun <F> ContextDependencies<F>.fillAccountData(
        out_AccountInfoToFill: DoubleArray,
        accontParams: BrokerAccountData
    ) =
        delay {
            val iBalance = 0
            val iTradeVal = 1
            val iMarginVal = 2
            with(accontParams) {
                out_AccountInfoToFill[iBalance] = balance
                out_AccountInfoToFill[iTradeVal] = tradeVal
                out_AccountInfoToFill[iMarginVal] = marginVal
            }
        }
}