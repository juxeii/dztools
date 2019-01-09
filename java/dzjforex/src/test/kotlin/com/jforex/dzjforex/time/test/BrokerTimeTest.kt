package com.jforex.dzjforex.time.test

import arrow.effects.fix
import com.dukascopy.api.IAccount
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.time.BrokerTimeApi.brokerTime
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk

class BrokerTimeTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val serverTime = 31455L

    private fun runBrokerTime() = contextApi
        .brokerTime()
        .fix()
        .unsafeRunSync()

    init {
        every { contextApi.jfContext.time } returns (serverTime)

        "BrokerTime returns new login required when not connected" {
            every { contextApi.client.isConnected } returns (false)

            val timeData = runBrokerTime()

            timeData.returnCode shouldBe CONNECTION_LOST_NEW_LOGIN_REQUIRED
        }

        "When client is connected" - {
            every { contextApi.client.isConnected } returns (true)

            "When market is closed return code is market closed"{
                every { contextApi.jfContext.dataService.isOfflineTime(serverTime) } returns (true)

                val timeData = runBrokerTime()

                timeData.returnCode shouldBe CONNECTION_OK_BUT_MARKET_CLOSED
                timeData.serverTime shouldBe serverTime.toUTCTime()
            }

            "When market is open" - {
                every { contextApi.jfContext.dataService.isOfflineTime(serverTime) } returns (false)

                "When no trading on account return code is trading not allowed"{
                    every { contextApi.account.accountState } returns (IAccount.AccountState.BLOCKED)

                    val timeData = runBrokerTime()

                    timeData.returnCode shouldBe CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
                    timeData.serverTime shouldBe serverTime.toUTCTime()
                }

                "When account is not blocked" - {
                    val testInstrumentA = mockk<Instrument>()
                    val testInstrumentB = mockk<Instrument>()
                    val subscribedInstruments = setOf(testInstrumentA, testInstrumentB)

                    every { contextApi.account.accountState } returns (IAccount.AccountState.OK)
                    every { contextApi.jfContext.subscribedInstruments } returns (subscribedInstruments)

                    "When no asset is tradeable return code is trading not allowed"{
                        every { contextApi.jfContext.engine.isTradable(any()) } returns (false)
                        every { testInstrumentA.isTradable } returns (false)
                        every { testInstrumentB.isTradable } returns (false)

                        val timeData = runBrokerTime()

                        timeData.returnCode shouldBe CONNECTION_OK_BUT_TRADING_NOT_ALLOWED
                        timeData.serverTime shouldBe serverTime.toUTCTime()
                    }

                    "When one asset is tradeable return code is connection OK"{
                        every { contextApi.jfContext.engine.isTradable(any()) } returns (false)
                        every { testInstrumentA.isTradable } returns (true)
                        every { testInstrumentB.isTradable } returns (false)

                        val timeData = runBrokerTime()

                        timeData.returnCode shouldBe CONNECTION_OK
                        timeData.serverTime shouldBe serverTime.toUTCTime()
                    }
                }
            }
        }
    }
}