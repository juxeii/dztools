package com.jforex.dzjforex.login

import com.dukascopy.api.system.IClient
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import org.apache.logging.log4j.LogManager

class BrokerLogin
{
    private val logger = LogManager.getLogger(BrokerLogin::class.java)

    fun login(client: IClient): Int
    {
        logger.debug("Starting login")
        val cred = LoginCredentials("DEMO3JHtFV", "JHtFV")
        val result = client
            .login(cred, LoginType.DEMO)
            .doOnError { logger.debug("Login failed with " + it.message) }
            .blockingGet()
        return if (result == null) 1 else 0
    }
}