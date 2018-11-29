package com.jforex.dzjforex.account

import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.IAccount
import com.jforex.dzjforex.misc.PluginEnvironment

internal fun <R> accountInfo(block: IAccount.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .account
            .run(block)
    }

internal fun lotSize() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.pluginSettings.lotSize() }

internal fun lotMargin() = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        val lotSize = lotSize().bind()
        val leverage = accountInfo { leverage }.bind()
        lotSize / leverage
    }.fix()

internal fun freeMargin() = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        val creditLine = accountInfo { creditLine }.bind()
        val leverage = accountInfo { leverage }.bind()
        creditLine / leverage
    }.fix()

internal fun tradeValue() = accountInfo { equity - baseEquity }

internal fun isTradingAllowedForAccount() = accountInfo {
    accountState == IAccount.AccountState.OK || accountState == IAccount.AccountState.OK_NO_MARGIN_CALL
}