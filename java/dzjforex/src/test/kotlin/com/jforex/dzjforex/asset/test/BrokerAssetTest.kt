package com.jforex.dzjforex.asset.test

import arrow.effects.fix
import com.dukascopy.api.*
import com.jforex.dzjforex.asset.BrokerAssetApi.brokerAsset
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.kforexutils.instrument.ask
import com.jforex.kforexutils.instrument.bid
import com.jforex.kforexutils.instrument.spread
import com.jforex.kforexutils.instrument.tick
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class BrokerAssetTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val assetName = "AUD/USD"
    private val testTick = mockk<ITick>()
    private val testInstrument = mockk<Instrument>()
    private val ask = 1.13456
    private val bid = 1.13444
    private val askVolume = 456.34
    private val spread = ask - bid
    private val pipCost = 0.08
    private val pipValue = 0.001
    private val minTradeAmount = 1000.0
    private val leverage = 100.0
    private val conversionRate = 1.2
    private val accountCurrency = JFCurrency.getInstance("EUR")

    private fun runBrokerAsset(assetName: String) = contextApi
        .brokerAsset(assetName)
        .fix()
        .unsafeRunSync()

    init {
        mockkStatic("com.jforex.kforexutils.instrument.InstrumentExtensionKt")
        mockkStatic(Instrument::class)
        every { Instrument.fromString(assetName) } returns testInstrument
        every { testTick.ask } returns ask
        every { testTick.bid } returns bid
        every { testInstrument.pipValue } returns pipValue
        every { testTick.askVolume } returns askVolume
        every { testInstrument.tick() } returns testTick
        every { testInstrument.ask() } returns ask
        every { testInstrument.bid() } returns bid
        every { testInstrument.spread() } returns spread
        every { testInstrument.minTradeAmount } returns minTradeAmount
        every { testInstrument.primaryJFCurrency } returns accountCurrency
        every { contextApi.account.leverage } returns leverage
        every { contextApi.account.accountCurrency } returns accountCurrency
        every {
            contextApi.jfContext.utils.getRate(
                accountCurrency,
                accountCurrency,
                OfferSide.ASK
            )
        } returns conversionRate
        every {
            contextApi.jfContext.utils.convertPipToCurrency(
                testInstrument,
                accountCurrency,
                OfferSide.ASK
            )
        } returns pipCost

        "BrokerAsset returns failing code when asset name is invalid" {
            val assetData = runBrokerAsset("invalid")

            assetData.returnCode shouldBe ASSET_UNAVAILABLE
        }

        "BrokerAsset returns failing code when parameter calculation fails" {
            every { testInstrument.spread() } throws JFException("TestExcpetion")

            val assetData = runBrokerAsset(assetName)

            assetData.returnCode shouldBe ASSET_UNAVAILABLE
        }

        "BrokerAsset calculations are OK" - {
            every { testInstrument.spread() } returns spread

            "BrokerAsset returns correct AssetData" {
                val assetData = runBrokerAsset(assetName)

                assetData.returnCode shouldBe ASSET_AVAILABLE
                assetData.lotAmount shouldBe minTradeAmount
                assetData.marginCost shouldBe 10.0
                assetData.pip shouldBe pipValue
                assetData.pipCost shouldBe 80.0
                assetData.price shouldBe ask
                assetData.spread shouldBe spread
                assetData.volume shouldBe askVolume
            }
        }
    }
}