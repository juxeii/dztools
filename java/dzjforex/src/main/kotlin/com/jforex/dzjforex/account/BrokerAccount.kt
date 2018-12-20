package com.jforex.dzjforex.account

import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.contextApi

object AccountApi
{
    fun ContextDependencies.isTradingAllowedForAccount() =
        account.accountState == IAccount.AccountState.OK ||
                account.accountState == IAccount.AccountState.OK_NO_MARGIN_CALL
}

/*
internal fun lotSize() = ReaderApi
    .ask<PluginConfig>()
    .map { config -> config.pluginSettings.lotSize() }

internal fun lotMargin() = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val lotSize = lotSize().bind()
        val leverage = getAccount { leverage }.bind()
        lotSize / leverage
    }.fix()

internal fun freeMargin() = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val creditLine = getAccount { creditLine }.bind()
        val leverage = getAccount { leverage }.bind()
        creditLine / leverage
    }.fix()

internal fun tradeValue() = getAccount { equity - baseEquity }

internal fun isTradingAllowedForAccount() = getAccount {
    accountState == IAccount.AccountState.OK || accountState == IAccount.AccountState.OK_NO_MARGIN_CALL
}*/
