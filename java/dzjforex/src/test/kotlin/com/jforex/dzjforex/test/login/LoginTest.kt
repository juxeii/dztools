package com.jforex.dzjforex.test.login

import arrow.effects.fix
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.test.mock.getPluginDependenciesForTest_IO
import com.jforex.dzjforex.zorro.LOGOUT_OK
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify

class LogoutTest : StringSpec()
{
    private val pluginApi = getPluginDependenciesForTest_IO()

    init
    {
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
