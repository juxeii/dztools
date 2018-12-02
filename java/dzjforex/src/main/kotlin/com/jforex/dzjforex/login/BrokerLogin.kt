package com.jforex.dzjforex.login

import arrow.core.toOption
import arrow.data.Reader
import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.instances.monad
import arrow.typeclasses.binding
import com.jforex.dzjforex.account.accountInfo
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.isConnected
import com.jforex.dzjforex.zorro.*
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal data class LoginData(
    val credentials: LoginCredentials,
    val accountType: String
)

internal fun loginToDukascopy(
    username: String,
    password: String,
    accountType: String
): Reader<PluginEnvironment, BrokerLoginResult> = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (isConnected().bind()) BrokerLoginResult(LOGIN_OK)
        else
        {
            val callResult = clientLogin(username, password, accountType).bind()
            val accountName = accountInfo { accountId }.bind().toOption()
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
        val loginData = LoginData(credentials, accountType)
        val loginType = getLoginType(loginData.accountType)
        val loginTask = createLoginTask(loginData, loginType).bind()
        progressWait(loginTask).bind()
    }

internal fun getLoginType(accountType: String) =
    if (accountType == realLoginType) LoginType.LIVE
    else LoginType.DEMO

internal fun createLoginTask(
    loginData: LoginData,
    loginType: LoginType
) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env.client
            .login(loginData.credentials, loginType)
            .toSingleDefault(LOGIN_OK)
            .doOnError { logger.debug("Login failed! " + it.message) }
            .onErrorReturnItem(LOGIN_FAIL)
            .map { loginResult ->
                if (loginResult == LOGIN_OK) env.pluginStrategy.start()
                loginResult
            }
    }

internal fun logoutFromDukascopy() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env.client.disconnect()
        LOGOUT_OK
    }