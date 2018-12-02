package com.jforex.dzjforex.login

import arrow.core.toOption
import arrow.data.Reader
import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.misc.getAccount
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.zorro.*
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun loginToDukascopy(
    username: String,
    password: String,
    accountType: String
): Reader<PluginEnvironment, BrokerLoginResult> = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (getClient { isConnected }.bind()) BrokerLoginResult(LOGIN_OK)
        else
        {
            val callResult = clientLogin(username, password, accountType).bind()
            val accountName = getAccount { accountId }.bind().toOption()
            BrokerLoginResult(callResult, accountName)
        }
    }.fix()

internal fun clientLogin(
    username: String,
    password: String,
    accountType: String
) = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        val credentials = LoginCredentials(username = username, password = password)
        val loginType = getLoginType(accountType)
        val loginTask = createLoginTask(credentials, loginType).bind()
        progressWait(loginTask).bind()
    }

internal fun getLoginType(accountType: String) =
    if (accountType == realLoginType) LoginType.LIVE
    else LoginType.DEMO

internal fun createLoginTask(
    loginCredentials: LoginCredentials,
    loginType: LoginType
) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env.client
            .login(loginCredentials, loginType)
            .toSingle {
                env.pluginStrategy.start()
                LOGIN_OK
            }
            .doOnError { logger.debug("Login failed! " + it.message) }
            .onErrorReturnItem(LOGIN_FAIL)
    }

internal fun logoutFromDukascopy() = getClient {
    disconnect()
    LOGOUT_OK
}