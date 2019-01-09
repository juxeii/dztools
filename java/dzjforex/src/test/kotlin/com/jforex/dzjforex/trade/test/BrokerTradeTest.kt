package com.jforex.dzjforex.trade.test

import arrow.effects.fix
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.trade.BrokerTradeApi.brokerTrade
import com.jforex.dzjforex.zorro.BROKER_ORDER_NOT_YET_FILLED
import com.jforex.dzjforex.zorro.BROKER_TRADE_FAIL
import com.jforex.dzjforex.zorro.lotScale
import com.jforex.kforexutils.instrument.ask
import com.jforex.kforexutils.instrument.bid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class BrokerTradeTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val testOrder = mockk<IOrder>()
    private val testInstrument = mockk<Instrument>()
    private val orderId = 123
    private val orderAmount = 0.123
    private val orderContracts = (orderAmount * lotScale).toInt()
    private val openPrice = 1.13456
    private val currentPL = 3.42
    private val ask = 1.22
    private val bid = 1.20

    private fun runBrokerTrade() = contextApi
        .brokerTrade(orderId)
        .fix()
        .unsafeRunSync()

    init {
        "BrokerTrade returns failing code when Id not found" {
            every { contextApi.engine.orders } returns (emptyList())
            every { contextApi.history.getHistoricalOrderById(orderId.toString()) } returns (null)

            val tradeData = runBrokerTrade()

            tradeData.returnCode shouldBe BROKER_TRADE_FAIL
        }

        "When order Id is found" - {
            every { testOrder.amount } returns (orderAmount)
            every { testOrder.isLong } returns (true)
            every { testOrder.id } returns (orderId.toString())
            every { testOrder.openPrice } returns (openPrice)
            every { testOrder.profitLossInAccountCurrency } returns (currentPL)
            mockkStatic("com.jforex.kforexutils.instrument.InstrumentExtensionKt")
            every { testInstrument.ask() } returns (ask)
            every { testInstrument.bid() } returns (bid)
            every { testOrder.instrument } returns (testInstrument)
            every { contextApi.engine.orders } returns (listOf(testOrder))

            "When order is filled the number of contracts is returned"{
                every { testOrder.state } returns (IOrder.State.FILLED)

                val tradeData = runBrokerTrade()

                tradeData.returnCode shouldBe orderContracts
            }

            "When order is closed the minus number of contracts is returned"{
                every { testOrder.state } returns (IOrder.State.CLOSED)

                val tradeData = runBrokerTrade()

                tradeData.returnCode shouldBe -orderContracts
            }

            "When order is opened return code is not yet filled"{
                every { testOrder.state } returns (IOrder.State.OPENED)

                val tradeData = runBrokerTrade()

                tradeData.returnCode shouldBe BROKER_ORDER_NOT_YET_FILLED
            }

            "When order is filled" - {
                every { testOrder.state } returns (IOrder.State.FILLED)

                "When order is long the trade parameters are correct"{
                    val tradeData = runBrokerTrade()

                    tradeData.close shouldBe bid
                    tradeData.open shouldBe openPrice
                    tradeData.profit shouldBe currentPL
                }

                "When order is short the trade parameters are correct"{
                    every { testOrder.isLong } returns (false)

                    val tradeData = runBrokerTrade()

                    tradeData.close shouldBe ask
                    tradeData.open shouldBe openPrice
                    tradeData.profit shouldBe currentPL
                }
            }

            "On some error BROKER_TRADE_FAIL is returned"{
                every { testOrder.state } throws JFException("Test error!")

                val tradeData = runBrokerTrade()

                tradeData.returnCode shouldBe BROKER_TRADE_FAIL
            }
        }
    }
}