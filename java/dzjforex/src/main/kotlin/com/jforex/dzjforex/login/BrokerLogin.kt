package com.jforex.dzjforex.login

import arrow.Kind
import com.jforex.dzjforex.init.BrokerInitApi.brokerInit
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.LOGOUT_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login

object LoginApi {
    fun <F> PluginDependencies<F>.brokerLogin(
        username: String,
        password: String,
        accountType: String
    ) = bindingCatch {
        if (!isConnected().bind()) {
            logger.debug("Starting login: username $username accountType $accountType")
            connect(username = username, password = password, accountType = accountType).bind()
            logger.debug("Successfully logged in.")
            brokerInit().bind()
        }
        BrokerLoginData(LOGIN_OK, contextApi.account.accountId)
    }.handleError { error ->
        natives.logAndPrintErrorOnZorro(
            "BrokerLogin failed! Error message: " +
                    "${error.message} " +
                    "Stack trace: ${getStackTrace(error)}"
        )
        BrokerLoginData(LOGIN_FAIL)
    }

    fun <F> PluginDependencies<F>.connect(
        username: String,
        password: String,
        accountType: String
    ): Kind<F, Unit> {
        val loginCredentials = LoginCredentials(username = username, password = password)
        val loginType = getLoginType(accountType)
        return client.login(loginCredentials, loginType, pluginSettings.useLoginPin(), this)
    }

    fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO

    fun <F> PluginDependencies<F>.logout() = delay {
        client.disconnect()
        LOGOUT_OK
    }
}