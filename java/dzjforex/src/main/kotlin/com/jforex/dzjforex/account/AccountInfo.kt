package com.jforex.dzjforex.account

import arrow.data.ReaderApi
import arrow.data.map
import arrow.data.runId
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.IAccount
import com.jforex.dzjforex.misc.PluginEnvironment

internal fun lotSize() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.pluginSettings.lotSize() }

internal fun leverage() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.pluginStrategy.account.leverage }

internal fun lotMargin() = ReaderApi
    .monad<PluginEnvironment>()
    .binding { lotSize().bind() / leverage().bind() }

internal fun tradeValue() = ReaderApi
    .ask<PluginEnvironment>()
    .map {
        with(it.pluginStrategy.account) {
            equity - baseEquity
        }
    }

internal fun freeMargin() = ReaderApi
    .ask<PluginEnvironment>()
    .map {
        with(it.pluginStrategy.account) {
            creditLine / leverage
        }
    }

internal fun accountCurrency() = ReaderApi
    .ask<PluginEnvironment>()
    .map {
        with(it.pluginStrategy.account) {
            accountCurrency
        }
    }

internal fun usedMargin() = ReaderApi
    .ask<PluginEnvironment>()
    .map {
        with(it.pluginStrategy.account) {
            equity - freeMargin().runId(it)
        }
    }

internal fun isTradingAllowedForAccount() = ReaderApi
    .ask<PluginEnvironment>()
    .map {
        with(it.pluginStrategy.account) {
            accountState === IAccount.AccountState.OK || accountState === IAccount.AccountState.OK_NO_MARGIN_CALL
        }
    }