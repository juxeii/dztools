package com.jforex.dzjforex.account

import arrow.typeclasses.ApplicativeError
import com.dukascopy.api.IAccount
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountApi.leverage
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi

lateinit var accountApi: AccountDependencies

fun initAccountApi()
{
    accountApi = AccountDependencies(pluginApi, contextApi)
}

interface AccountDependencies : PluginDependencies, ContextDependencies
{
    companion object
    {
        operator fun invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies
        ): AccountDependencies =
            object : AccountDependencies,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies
            {}
    }
}

object AccountApi
{
    fun AccountDependencies.leverage() = account.leverage

    fun AccountDependencies.equity() = account.equity

    fun AccountDependencies.baseEquity() = account.baseEquity

    fun AccountDependencies.state() = account.accountState

    fun AccountDependencies.lotSize() = pluginSettings.lotSize()

    fun AccountDependencies.lotMargin() = lotSize() / leverage()

    fun AccountDependencies.tradeValue() = equity() - baseEquity()

    fun AccountDependencies.freeMargin() = account.creditLine / leverage()

    fun AccountDependencies.isNFAAccount() = account.isGlobal

    fun AccountDependencies.pipCost(instrument: Instrument) = context
        .utils
        .convertPipToCurrency(instrument, account.accountCurrency) * lotSize()

    fun AccountDependencies.isTradingAllowed() =
        state() == IAccount.AccountState.OK || state() == IAccount.AccountState.OK_NO_MARGIN_CALL

}
