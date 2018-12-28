package com.jforex.dzjforex.account

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE

object BrokerAccountApi
{
    fun <F> ContextDependencies<F>.brokerAccount(out_AccountInfoToFill: DoubleArray): Kind<F, Int> = bindingCatch {
        if (!account.isConnected) ACCOUNT_UNAVAILABLE
        else
        {
            fillAccountValues(out_AccountInfoToFill).bind()
            ACCOUNT_AVAILABLE
        }
    }

    fun <F> ContextDependencies<F>.fillAccountValues(out_AccountInfoToFill: DoubleArray): Kind<F, Unit> = catch {
        val iBalance = 0
        val iTradeVal = 1
        val iMarginVal = 2

        out_AccountInfoToFill[iBalance] = account.baseEquity
        out_AccountInfoToFill[iTradeVal] = account.equity - account.baseEquity
        out_AccountInfoToFill[iMarginVal] = account.usedMargin
    }
}