package com.jforex.dzjforex.account

import arrow.effects.ForIO
import com.dukascopy.api.IAccount
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.order.OrderRepositoryApi.getZorroOrders
import com.jforex.kforexutils.misc.toAmount

object AccountApi
{
    fun <F> ContextDependencies<F>.leverage() = account.leverage

    fun <F> ContextDependencies<F>.equity() = account.equity

    fun <F> ContextDependencies<F>.baseEquity() = account.baseEquity

    fun <F> ContextDependencies<F>.state(): IAccount.AccountState = account.accountState

    fun <F> ContextDependencies<F>.lotSize() = pluginSettings.lotSize()

    fun <F> ContextDependencies<F>.lotMargin() = lotSize() / leverage()

    fun <F> ContextDependencies<F>.freeMargin() = account.creditLine / leverage()

    fun <F> ContextDependencies<F>.pipCost(instrument: Instrument) = context
        .utils
        .convertPipToCurrency(instrument, account.accountCurrency) * lotSize()

    fun <F> ContextDependencies<F>.isTradingAllowed() =
        state() == IAccount.AccountState.OK || state() == IAccount.AccountState.OK_NO_MARGIN_CALL

}
