package com.jforex.dzjforex.buy.test

import arrow.effects.fix
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.buy.BrokerBuyApi.brokerBuy
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class BrokerBuyTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val assetName = "EUR/USD"
    private val testOrder = mockk<IOrder>()
    private val testInstrument = mockk<Instrument>()
    private val slDistance: Double = 0.00345
    private val contracts = 12300
    private val amount = 0.0123
    private val slippage = 6.0
    private val limitPrice = 1.12345
    private val orderText = "orderText"

    private fun runBrokerBuy(assetName: String) = contextApi
        .brokerBuy(
            assetName = assetName,
            contracts = contracts,
            slDistance = slDistance,
            slippage = slippage,
            limitPrice = limitPrice,
            orderText = orderText
        )
        .fix()
        .unsafeRunSync()

    init {
        every { testOrder.instrument } returns testInstrument
        every { testInstrument.pipScale } returns 4
        every { testInstrument.pipValue } returns 0.08

        "BrokerBuy returns failing code when asset name is invalid" {
            val buyData = runBrokerBuy("invalid")

            buyData.returnCode shouldBe BROKER_BUY_FAIL
        }

        "When assetName is valid" - {
            mockkStatic(Instrument::class)
            every { Instrument.fromString(assetName) } returns testInstrument

            "BrokerBuy returns failing code when instrument is not tradeable" {
                every { testInstrument.isTradable } returns false

                val buyData = runBrokerBuy(assetName)

                buyData.returnCode shouldBe BROKER_BUY_FAIL
            }
        }
    }
}