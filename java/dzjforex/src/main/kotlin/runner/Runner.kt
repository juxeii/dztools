package runner

import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.login.BrokerLogin
import com.jforex.dzjforex.misc.getClient
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

fun main(args : Array<String>) {
    val client = getClient()

    logger.debug("Starting login")
    val cred = LoginCredentials("DEMO3JHtFV", "JHtFV")
    //val cred = LoginCredentials("Zematis716EU", "Charon69")
    val result = client
        .login(cred, LoginType.DEMO)
        .doOnError { logger.debug("Login failed with " + it.message) }
        .blockingGet()
}