package com.jforex.dzjforex.account

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
    fun <F> ContextDependencies<F>.brokerAccount() =
        createAccountData().handleErrorWith { error -> processError(error) }

    fun <F> ContextDependencies<F>.createAccountData() =
        map(baseEequity(), tradeVal(), usedMargin()) {
            val accountData = BrokerAccountData(
                returnCode = ACCOUNT_AVAILABLE,
                balance = it.a,
                tradeVal = it.b,
                marginVal = it.c
            )
            logger.debug("$accountData")
            accountData
        }

    fun <F> ContextDependencies<F>.processError(error: Throwable) = delay {
        logger.error(
            "BrokerAccount failed! " +
                    "Error message: ${error.message} " +
                    "Stack trace: ${getStackTrace(error)}"
        )
        BrokerAccountData(ACCOUNT_UNAVAILABLE)
    }
}