package com.jforex.dzjforex.account

import arrow.Kind
import com.jforex.dzjforex.account.AccountApi.baseEquity
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

    /*fun <F> ContextDependencies<F>.tradeValue(): Double =
        getZorroOrders().map { orders ->
            orders
                .stream()
                .mapToDouble { it.profitLossInAccountCurrency }
                .sum()
                .toAmount()
        }.fold({ throw(JFException("Could not calculate trade value! ${it.message}")) }) { it }*/

    fun <F> ContextDependencies<F>.tradeValue(): Double =
        account.equity - account.baseEquity
}