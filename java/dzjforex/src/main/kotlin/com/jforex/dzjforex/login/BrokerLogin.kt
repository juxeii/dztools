package com.jforex.dzjforex.login

import arrow.data.*
import arrow.instances.monad
import arrow.typeclasses.binding
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
    accountType: String,
    out_AccountNamesToFill: Array<String>
):Reader<PluginEnvironment, Int> = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        if (isConnected().bind()) LOGIN_OK
        else
        {
            val credentials = LoginCredentials(username = username, password = password)
            val loginData = LoginData(credentials, accountType)
            val loginType = getLoginType(loginData.accountType)
            clientLogin(loginData, loginType, out_AccountNamesToFill).bind()
        }
    }.fix()

internal fun clientLogin(
    loginData: LoginData,
    loginType: LoginType,
    out_AccountNamesToFill: Array<String>
) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env.client
            .login(loginData.credentials, loginType)
            .toSingleDefault(LOGIN_OK)
            .doOnError { logger.debug("Login failed! " + it.message) }
            .onErrorReturnItem(LOGIN_FAIL)
            .map { loginResult ->
                if (loginResult == LOGIN_OK)
                {
                    env.pluginStrategy.start()
                    out_AccountNamesToFill[0] = env.pluginStrategy.account.accountId
                }
                loginResult
            }
    }
    .flatMap { progressWait(it) }

fun getLoginType(accountType: String) =
    if (accountType == realLoginType) LoginType.LIVE
    else LoginType.DEMO

fun logoutFromDukascopy() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env.client.disconnect()
        LOGOUT_OK
    }