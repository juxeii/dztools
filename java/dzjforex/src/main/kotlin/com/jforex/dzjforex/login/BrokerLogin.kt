package com.jforex.dzjforex.login

import arrow.data.ReaderApi
import arrow.data.map
import arrow.data.runId
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.LOGOUT_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.client.logout
import io.reactivex.Completable
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun loginToDukascopy(loginData: LoginData) = ReaderApi
    .ask<IClient>()
    .map { client ->
        if (client.isConnected) Single.just(LOGIN_OK)
        else clientLogin(loginData)
            .runId(client)
            .doOnComplete { logger.debug("login completed") }
            .toSingleDefault(LOGIN_OK)
            .doOnError { logger.debug("Login exception! " + it.message) }
            .onErrorReturnItem(LOGIN_FAIL)
    }

private fun clientLogin(loginData: LoginData) = ReaderApi
    .ask<IClient>()
    .map { client ->
        val loginCredentials = LoginCredentials(
            username = loginData.username,
            password = loginData.password
        )
        val loginType = getLoginType(loginData.accountType)
        client.login(loginCredentials, loginType)
    }

private fun getLoginType(accountType: String) =
    if (accountType == realLoginType) LoginType.LIVE
    else LoginType.DEMO

internal fun logoutFromDukascopy() = ReaderApi
    .ask<IClient>()
    .map { client ->
        Completable
            .fromCallable { client.logout() }
            .toSingleDefault(LOGOUT_OK)
    }