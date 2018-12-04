package com.jforex.dzjforex.account

import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.IAccount
import com.jforex.dzjforex.misc.PluginConfig
import com.jforex.dzjforex.misc.PluginConfigExt
import com.jforex.dzjforex.misc.getAccount

internal fun lotSize() = ReaderApi
    .ask<PluginConfigExt>()
    .map { env -> env.pluginConfig.pluginSettings.lotSize() }

internal fun lotMargin() = ReaderApi
    .monad<PluginConfigExt>()
    .binding {
        val lotSize = lotSize().bind()
        val leverage = getAccount { leverage }.bind()
        lotSize / leverage
    }.fix()

internal fun freeMargin() = ReaderApi
    .monad<PluginConfigExt>()
    .binding {
        val creditLine = getAccount { creditLine }.bind()
        val leverage = getAccount { leverage }.bind()
        creditLine / leverage
    }.fix()

internal fun tradeValue() = getAccount { equity - baseEquity }

internal fun isTradingAllowedForAccount() = getAccount {
    accountState == IAccount.AccountState.OK || accountState == IAccount.AccountState.OK_NO_MARGIN_CALL
}