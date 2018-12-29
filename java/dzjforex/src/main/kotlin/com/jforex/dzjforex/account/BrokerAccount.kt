package com.jforex.dzjforex.account

import arrow.Kind
import arrow.typeclasses.binding
import arrow.typeclasses.bindingCatch
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.account.AccountApi.baseEequity
import com.jforex.dzjforex.account.AccountApi.tradeVal
import com.jforex.dzjforex.account.AccountApi.usedMargin

data class BrokerAccountData(val balance: Double, val tradeVal: Double, val marginVal: Double)
sealed class BrokerAccountResult(val returnCode: Int) {
    data class Failure(val code: Int) : BrokerAccountResult(code)
    data class Success(val code: Int, val data: BrokerAccountData) : BrokerAccountResult(code)
}
typealias BrokerAccountFailure = BrokerAccountResult.Failure
typealias BrokerAccountSuccess = BrokerAccountResult.Success

object BrokerAccountApi {
    fun <F> ContextDependencies<F>.brokerAccount(): Kind<F, BrokerAccountResult> = binding {
        if (!isConnected().bind()) BrokerAccountFailure(ACCOUNT_UNAVAILABLE)
        else BrokerAccountSuccess(ACCOUNT_AVAILABLE, getAccountData().bind())
    }

    fun <F> ContextDependencies<F>.getAccountData(): Kind<F, BrokerAccountData> = binding {
        BrokerAccountData(
            balance = baseEequity().bind(),
            tradeVal = tradeVal().bind(),
            marginVal = usedMargin().bind()
        )
    }
}