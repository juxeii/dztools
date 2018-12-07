package com.jforex.dzjforex.login

import arrow.core.Try
import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login

internal fun login(
    username: String,
    password: String,
    accountType: String
) = ReaderApi
    .monad<IClient>()
    .binding {
        val loginCredentials = LoginCredentials(username = username, password = password)
        val loginType = getLoginType(accountType)
        tryLogin(loginCredentials, loginType).bind()
    }.fix()

internal fun getLoginType(accountType: String) =
    if (accountType == realLoginType) LoginType.LIVE
    else LoginType.DEMO

internal fun tryLogin(
    loginCredentials: LoginCredentials,
    loginType: LoginType
) = ReaderApi
    .ask<IClient>()
    .map { client -> Try { client.login(loginCredentials, loginType).blockingAwait() } }