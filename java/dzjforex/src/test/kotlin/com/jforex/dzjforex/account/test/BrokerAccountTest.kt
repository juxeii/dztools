package com.jforex.dzjforex.account.test

import arrow.effects.fix
import com.dukascopy.api.JFException
import com.jforex.dzjforex.account.BrokerAccountApi.brokerAccount
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.zorro.ACCOUNT_AVAILABLE
import com.jforex.dzjforex.zorro.ACCOUNT_UNAVAILABLE
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every

private val contextApi = getContextDependenciesForTest_IO()

class BrokerAccountTest : StringSpec() {

    private val equity = 31.3545
    private val baseEquity = 12.473487
    private val usedMargin = 3457.2343

    private fun runBrokerAccount() = contextApi
        .brokerAccount()
        .fix()
        .unsafeRunSync()

    init {
        every { contextApi.account.equity } returns (equity)
        every { contextApi.account.usedMargin } returns (usedMargin)

        "BrokerAccount data is correctly filled" {
            every { contextApi.account.baseEquity } returns (baseEquity)

            val accountData = runBrokerAccount()

            accountData.returnCode shouldBe ACCOUNT_AVAILABLE
            accountData.balance shouldBe baseEquity
            accountData.tradeVal shouldBe (equity - baseEquity)
            accountData.marginVal shouldBe usedMargin
        }

        "On some error ACCOUNT_UNAVAILABLE is returned" {
            every { contextApi.account.baseEquity } throws JFException("BrokerAccount excpetion")

            val accountData = runBrokerAccount()

            accountData.returnCode shouldBe ACCOUNT_UNAVAILABLE
        }
    }
}