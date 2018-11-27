package com.jforex.dzjforex.login

import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.zorro.LOGIN_FAIL
import com.jforex.dzjforex.zorro.LOGIN_OK
import com.jforex.dzjforex.zorro.LOGOUT_OK
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.client.logout
import io.reactivex.Completable
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

internal class BrokerLogin(private val client: IClient)
{
    private val logger = LogManager.getLogger(BrokerLogin::class.java)

    fun login(loginData: LoginData): Single<Int> =
        if (client.isConnected) Single.just(LOGIN_OK)
        else clientLogin(loginData)
            .toSingleDefault(LOGIN_OK)
            .doOnError { logger.debug("Login exception! " + it.message) }
            .onErrorReturnItem(LOGIN_FAIL)

    private fun clientLogin(loginData: LoginData): Completable
    {
        val loginCredentials = LoginCredentials(
            username = loginData.username,
            password = loginData.password
        )
        val loginType = getLoginType(loginData.accountType)

        return client.login(loginCredentials, loginType)
    }

    private fun getLoginType(accountType: String) =
        if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO

    fun logout(): Single<Int> = Completable
        .fromCallable { client.logout() }
        .toSingleDefault(LOGOUT_OK)
}