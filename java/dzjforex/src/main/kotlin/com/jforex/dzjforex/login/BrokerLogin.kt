package com.jforex.dzjforex.login

import arrow.Kind
import arrow.effects.IO
import arrow.effects.instances.io.monadDefer.monadDefer
import arrow.effects.typeclasses.MonadDefer
import arrow.typeclasses.bindingCatch
import com.jforex.dzjforex.history.initHistoryApi
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.time.initBrokerTimeApi
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

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
            val loginCredentials = LoginCredentials(username = username, password = password)
            val loginType = getLoginType(accountType)
            client.login(loginCredentials, loginType, this@create)

            initComponents(KForexUtilsStrategy()).bind()
            out_AccountNamesToFill[0] = contextApi.account.accountId
        }
        LOGIN_OK
    }.handleError {
        logger.debug("Login failed! $it")
        LOGIN_FAIL
    }

    fun <F> LoginDependencies<F>.initComponents(strategy: KForexUtilsStrategy): Kind<F, Unit> = catch {
        client.startStrategy(strategy)
        val kForexUtils = strategy.kForexUtilsSingle().blockingFirst()
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { saveQuote(it) })
        initContextApi(kForexUtils.context)
        initHistoryApi()
        initBrokerTimeApi()
    }

    fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO
}