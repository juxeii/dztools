package com.jforex.dzjforex.login

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.order.initOrderRepositoryApi
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.LOGOUT_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy

object LoginApi
{
    fun <F> PluginDependencies<F>.brokerLogin(
        username: String,
        password: String,
        accountType: String
    ): Kind<F, Int> = bindingCatch {
        if (!isConnected().bind()) connect(username, password, accountType).bind()
        LOGIN_OK
    }.handleError { loginError ->
        logger.debug("Login failed! Error: $loginError Stack trace: ${getStackTrace(loginError)}")
        LOGIN_FAIL
    }

    private fun <F> PluginDependencies<F>.connect(
        username: String,
        password: String,
        accountType: String
    ): Kind<F, Unit>
    {
        val loginCredentials = LoginCredentials(username = username, password = password)
        val loginType = getLoginType(accountType)
        return client.login(loginCredentials, loginType, this)
    }

    private fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO

    fun <F> PluginDependencies<F>.logout(): Kind<F, Int> = invoke {
        client.disconnect()
        LOGOUT_OK
    }
}