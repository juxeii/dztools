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
import com.jforex.dzjforex.zorro.LOGOUT_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

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
            logger.debug("Login 1")
            val loginCredentials = LoginCredentials(username = username, password = password)
            logger.debug("Login 2")
            val loginType = getLoginType(accountType)
            logger.debug("Login 3")
            client.login(loginCredentials, loginType, this@create)
            logger.debug("Login 4")

            initComponents(KForexUtilsStrategy()).bind()
            logger.debug("Login 5")


            if(contextApi == null){
                logger.debug("Login cnull")
            }
            if(contextApi.account == null){
                logger.debug("Login accnull")
            }
            out_AccountNamesToFill[0] = contextApi.account.accountId
        }
        LOGIN_OK
    }.handleError {
        logger.debug("Login failed! $it")
        LOGIN_FAIL
    }

    fun <F> LoginDependencies<F>.initComponents(strategy: KForexUtilsStrategy): Kind<F, Unit> = catch {
        logger.debug("initComponents 1")

        if(client == null){
            logger.debug("client is null!!")
        }
        if(strategy == null){
            logger.debug("strategy is null!!")
        }

        client.startStrategy(strategy)
        logger.debug("initComponents 2")
        val kForexUtils = strategy.kForexUtilsSingle().blockingFirst()
        logger.debug("initComponents 3")
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { saveQuote(it) })
        logger.debug("initComponents 4")
        initContextApi(kForexUtils.context)
        logger.debug("initComponents 5")
        initHistoryApi()
        logger.debug("initComponents 6")
        initBrokerTimeApi()
    }

    fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO

    fun <F> LoginDependencies<F>.logout(): Int {
        client.disconnect()
        return LOGOUT_OK
    }
}