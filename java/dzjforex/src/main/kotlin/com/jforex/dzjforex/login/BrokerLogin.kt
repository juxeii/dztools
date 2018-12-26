package com.jforex.dzjforex.login

import arrow.Kind
import arrow.effects.IO
import arrow.effects.instances.io.monadDefer.monadDefer
import arrow.effects.typeclasses.MonadDefer
import arrow.typeclasses.bindingCatch
import com.jforex.dzjforex.account.initAccountApi
import com.jforex.dzjforex.buy.initBrokerBuyApi
import com.jforex.dzjforex.history.initHistoryApi
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.initOrderRepositoryApi
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.LOGOUT_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy

val loginApi = LoginDependencies(pluginApi, IO.monadDefer())

interface LoginDependencies<F> : PluginDependencies, MonadDefer<F>
{
    companion object
    {
        operator fun <F> invoke(pluginDependencies: PluginDependencies, MD: MonadDefer<F>): LoginDependencies<F> =
            object : LoginDependencies<F>, MonadDefer<F> by MD, PluginDependencies by pluginDependencies
            {}
    }
}

object LoginApi
{
    fun <F> LoginDependencies<F>.create(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Kind<F, Int> = bindingCatch {
        if (!client.isConnected)
        {
            connect(username, password, accountType).bind()
            initComponents().bind()
            out_AccountNamesToFill[0] = contextApi.account.accountId
        }
        LOGIN_OK
    }.handleError {
        logger.debug("Login failed! $it")
        LOGIN_FAIL
    }

    private fun <F> LoginDependencies<F>.connect(
        username: String,
        password: String,
        accountType: String
    ): Kind<F, Unit>
    {
        val loginCredentials = LoginCredentials(username = username, password = password)
        val loginType = getLoginType(accountType)
        return client.login(loginCredentials, loginType, this)
    }

    fun <F> LoginDependencies<F>.initComponents(): Kind<F, Unit> = catch {
        val strategy = KForexUtilsStrategy()
        client.startStrategy(strategy)
        val kForexUtils = strategy.kForexUtilsSingle().blockingFirst()
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { saveQuote(it) })
        initContextApi(kForexUtils.context)
        initHistoryApi()
        initAccountApi()
        initBrokerBuyApi()
        initOrderRepositoryApi()
    }

    fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO

    fun <F> LoginDependencies<F>.logout(): Int
    {
        client.disconnect()
        return LOGOUT_OK
    }
}