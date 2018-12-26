package com.jforex.dzjforex.account

import arrow.Kind
import com.jforex.dzjforex.account.AccountApi.baseEquity
import com.jforex.dzjforex.account.AccountApi.tradeValue
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE

object BrokerAccountApi
{
    fun <F> ContextDependencies<F>.brokerAccount(out_AccountInfoToFill: DoubleArray): Kind<F, Int> = catch {
        if (!account.isConnected) ACCOUNT_UNAVAILABLE
        else
        {
            out_AccountInfoToFill[0] = baseEquity()
            out_AccountInfoToFill[1] = tradeValue()
            out_AccountInfoToFill[2] = account.usedMargin
            ACCOUNT_AVAILABLE
        }
    }
}