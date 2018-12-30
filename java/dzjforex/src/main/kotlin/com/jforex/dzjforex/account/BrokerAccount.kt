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

data class BrokerAccountData(val balance: Double, val tradeVal: Double, val marginVal: Double)
sealed class BrokerAccountResult(val returnCode: Int)
{
    data class Failure(val code: Int) : BrokerAccountResult(code)
    data class Success(val code: Int, val data: BrokerAccountData) : BrokerAccountResult(code)
}
typealias BrokerAccountFailure = BrokerAccountResult.Failure
typealias BrokerAccountSuccess = BrokerAccountResult.Success

object BrokerAccountApi
{
    fun <F> ContextDependencies<F>.brokerAccount(): Kind<F, BrokerAccountResult> =
        getAccountData()
            .map { accountData -> BrokerAccountSuccess(ACCOUNT_AVAILABLE, accountData) }
            .handleError { error ->
                logger.error("BrokerAccount failed! Error: $error Stack trace: ${getStackTrace(error)}")
                BrokerAccountFailure(ACCOUNT_UNAVAILABLE)
            }

    fun <F> ContextDependencies<F>.getAccountData(): Kind<F, BrokerAccountData> =
        map(baseEequity(), tradeVal(), usedMargin())
        { BrokerAccountData(balance = it.a, tradeVal = it.b, marginVal = it.c) }
}