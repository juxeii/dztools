package com.jforex.dzjforex.subscribe.test

import arrow.effects.fix
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFCurrency
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.subscribe.BrokerSubscribeApi.brokerSubscribe
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.*

class BrokerSubscribeTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val assetName = "AUD/USD"
    private val subscribedInstruments = setOf(Instrument.AUDCAD)

    override fun isInstancePerTest(): Boolean = true

    private fun runBrokerSubscribe(assetName: String) = contextApi
        .brokerSubscribe(assetName)
        .fix()
        .unsafeRunSync()

    init {
        "BrokerSubscribe returns failing code when asset name is invalid" {
            val subscribeResult = runBrokerSubscribe("invalid")

            subscribeResult shouldBe SUBSCRIBE_FAIL
        }

        /*"When asset name is valid" - {
            mockkStatic(Instrument::class)
            every { Instrument.fromString(assetName) } returns Instrument.AUDUSD

            every { contextApi.account.accountCurrency } returns JFCurrency.getInstance("EUR")
            every { contextApi.jfContext.setSubscribedInstruments(any(), false) } just Runs
            every { contextApi.jfContext.subscribedInstruments } returns (subscribedInstruments)

            "Instrument and cross instruments are subscribed"{
                val toSubscribeInstruments = setOf(
                    Instrument.AUDUSD,
                    Instrument.EURAUD,
                    Instrument.EURUSD,
                    Instrument.AUDCAD
                )

                val subscribeResult = runBrokerSubscribe(assetName)

                subscribeResult shouldBe SUBSCRIBE_OK

                verify {
                    contextApi.jfContext.setSubscribedInstruments(match { it == toSubscribeInstruments }, false)
                }
            }
        }*/
    }
}