package com.jforex.dzjforex.account

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.ApplicativeError
import com.jforex.dzjforex.account.AccountApi.baseEquity
import com.jforex.dzjforex.account.AccountApi.tradeValue
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE

lateinit var brokerAccountApi: BrokerAccountDependencies<ForIO>

fun initBrokerAccountApi()
{
    brokerAccountApi = BrokerAccountDependencies(accountApi, IO.monadError())
}

interface BrokerAccountDependencies<F> : AccountDependencies, ApplicativeError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            accountDependencies: AccountDependencies,
            AE: ApplicativeError<F, Throwable>
        ): BrokerAccountDependencies<F> =
            object : BrokerAccountDependencies<F>,
                AccountDependencies by accountDependencies,
                ApplicativeError<F, Throwable> by AE
            {}
    }
}

object BrokerAccountApi
{
    fun <F> BrokerAccountDependencies<F>.create(out_AccountInfoToFill: DoubleArray):Kind<F, Int> = catch {
        if(!account.isConnected) ACCOUNT_UNAVAILABLE
        else{
            out_AccountInfoToFill[0] = baseEquity()
            out_AccountInfoToFill[1] = tradeValue()
            out_AccountInfoToFill[2] = account.usedMargin
            ACCOUNT_AVAILABLE
        }
    }
}