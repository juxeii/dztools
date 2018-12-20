package com.jforex.dzjforex.login

import arrow.Kind
import arrow.effects.typeclasses.MonadDefer
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

interface LoginDependencies<F> : MonadDefer<F>
{
    val client: IClient

    companion object
    {
        operator fun <F> invoke(MD: MonadDefer<F>, client: IClient): LoginDependencies<F> =
            object : LoginDependencies<F>, MonadDefer<F> by MD
            {
                override val client = client
            }
    }
}

object LoginApi
{
    fun <F> LoginDependencies<F>.login(
        username: String,
        password: String,
        accountType: String
    ): Kind<F, Unit>
    {
        val loginCredentials = LoginCredentials(username = username, password = password)
        val loginType = getLoginType(accountType)
        return client.login(
            loginCredentials,
            loginType,
            this
        )
    }

    fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO
}