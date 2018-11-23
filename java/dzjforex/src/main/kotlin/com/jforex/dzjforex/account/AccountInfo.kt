package com.jforex.dzjforex.account

import com.dukascopy.api.IAccount
import com.jforex.dzjforex.settings.PluginSettings

class AccountInfo(
    private val account: IAccount,
    private val pluginSettings: PluginSettings
) : IAccount by account
{
    fun lotSize() = pluginSettings.lotSize()

    fun lotMargin() = lotSize() / leverage

    fun tradeValue() = equity - baseEquity

    fun freeMargin() = creditLine / leverage

    fun usedMargin() = equity - freeMargin()

    fun isTradingAllowed() =
        accountState === IAccount.AccountState.OK || accountState === IAccount.AccountState.OK_NO_MARGIN_CALL
}