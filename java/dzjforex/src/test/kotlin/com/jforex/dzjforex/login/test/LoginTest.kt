package com.jforex.dzjforex.login.test

import arrow.effects.fix
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.mock.test.getPluginDependenciesForTest_IO
import com.jforex.dzjforex.zorro.LOGOUT_OK
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify

private val pluginApi = getPluginDependenciesForTest_IO()
private val contextApi = getContextDependenciesForTest_IO()

class LoginTest : StringSpec() {
    val username = "John_Doe"
    val password = "123456"
    val accountName = "JohnAccount42"

    init {
        /*"Login is OK when client is already connected" {
            val accountType = demoLoginType

            every { pluginApi.client.isConnected } returns (false)
            every { contextApi.account.accountId } returns (accountName)
            val loginResult = pluginApi
                .brokerLogin(username = username, password = password, accountType = accountType)
                .fix()
                .unsafeRunSync()

            verify(exactly = 1) { pluginApi.client.isConnected }
            loginResult.returnCode shouldBe LOGIN_OK
            loginResult.accountName shouldBe accountName

            //confirmVerified(pluginApi.client)
        }*/

        /*"Login is called with correct parameters" {
            val username = "John_Doe"
            val password = "123456"
            val accountType = demoLoginType

            every { pluginApi.client.disconnect() } just Runs
            val loginResult = pluginApi
                .brokerLogin(username = username, password = password, accountType = accountType)
                .fix()
                .unsafeRunSync()

            verify(exactly = 1) {
                pluginApi.client.login(
                    match {
                        it.password == password && it.username == username
                    },
                    eq(LoginType.DEMO),
                    IO.monadDefer()
                )
            }
            // logoutResult.shouldBe(LOGOUT_OK)
        }*/
    }
}

class LogoutTest : StringSpec() {
    init {
        "Disconnect is called on client instance" {
            every { pluginApi.client.disconnect() } just Runs
            val logoutResult = pluginApi
                .logout()
                .fix()
                .unsafeRunSync()

            verify(exactly = 1) { pluginApi.client.disconnect() }
            logoutResult.shouldBe(LOGOUT_OK)
        }
    }
}
