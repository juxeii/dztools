package com.jforex.dzjforex.account

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE

data class BrokerAccountData(val balance: Double, val tradeVal: Double, val marginVal: Double)
sealed class BrokerAccountResult(val returnCode: Int) {
    data class Failure(val code: Int) : BrokerAccountResult(code)
    data class Success(val code: Int, val data: BrokerAccountData) : BrokerAccountResult(code)
}
typealias BrokerAccountFailure = BrokerAccountResult.Failure
typealias BrokerAccountSuccess = BrokerAccountResult.Success

object BrokerAccountApi {
    fun <F> ContextDependencies<F>.brokerAccount(): Kind<F, BrokerAccountResult> = invoke {
        if (!account.isConnected) BrokerAccountFailure(ACCOUNT_UNAVAILABLE)
        else {
            val accountData = BrokerAccountData(
                balance = account.baseEquity,
                tradeVal = account.equity - account.baseEquity,
                marginVal = account.usedMargin
            )
            BrokerAccountSuccess(ACCOUNT_AVAILABLE, accountData)
        }
    }
}