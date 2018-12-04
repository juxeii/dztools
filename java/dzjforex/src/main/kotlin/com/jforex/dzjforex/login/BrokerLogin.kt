package com.jforex.dzjforex.login

import arrow.data.Reader
import com.jforex.dzjforex.misc.PluginConfig
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getLoginTask(
    username: String,
    password: String,
    accountType: String
): Reader<PluginConfig, Single<Int>> {
    val credentials = LoginCredentials(username = username, password = password)
    val loginType = getLoginType(accountType)
    return createLoginTask(credentials, loginType)
}

internal fun getLoginType(accountType: String) =
    if (accountType == realLoginType) LoginType.LIVE
    else LoginType.DEMO

internal fun createLoginTask(
    loginCredentials: LoginCredentials,
    loginType: LoginType
) = getClient {
    login(loginCredentials, loginType)
        .toSingleDefault(LOGIN_OK)
        .doOnError { logger.debug("Login failed! " + it.message) }
        .onErrorReturnItem(LOGIN_FAIL)
}