package com.jforex.dzjforex.sell.test

import arrow.core.Some
import arrow.effects.fix
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.sell.BrokerSellApi.brokerSell
import com.jforex.dzjforex.zorro.BROKER_SELL_FAIL
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class BrokerSellTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val testOrder = mockk<IOrder>()
    private val testInstrument = mockk<Instrument>()
    private val orderId = 123
    private val slPrice = 1.13456
    private val contracts = 120000
    private val slippage = 6.0
    private val maybeLimitPrice = Some(1.12345)

    private fun runBrokerSell() = contextApi
        .brokerSell(orderId = orderId, contracts = contracts, maybeLimitPrice = maybeLimitPrice, slippage = slippage)
        .fix()
        .unsafeRunSync()

    init {
        every { testOrder.instrument } returns testInstrument
        every { testOrder.id } returns (orderId.toString())

        "BrokerSell returns failing code when Id not found" {
            every { contextApi.engine.orders } returns (emptyList())
            every { contextApi.history.getHistoricalOrderById(orderId.toString()) } returns (null)

            val sellResult = runBrokerSell()

            sellResult shouldBe BROKER_SELL_FAIL
        }

        "When order Id is found" - {
            every { contextApi.engine.orders } returns (listOf(testOrder))

            "BrokerSell returns failing code when instrument is not tradeable" {
                every { testInstrument.isTradable } returns (false)

                val sellResult = runBrokerSell()

                sellResult shouldBe BROKER_SELL_FAIL
            }

            "When order instrument is tradeable" - {
                every { testInstrument.isTradable } returns (true)
                mockkStatic("com.jforex.kforexutils.order.extension.OrderCloseExtensionKt")

                "When close call fails failing code is returned"{
                    //every { testOrder.setSL(any(), any(), any(), any()) } throws JFException("Test exception")

                    //val sellResult = runBrokerSell()

                    //sellResult shouldBe BROKER_SELL_FAIL
                }

                /*"When stop loss call succeeds" - {
                    fun setSLEvent(type: OrderEventType) {
                        val orderEvent = OrderEvent(testOrder, type)
                        every { testOrder.setSL(slPrice, any(), any(), any()) } returns Observable.just(orderEvent)
                    }

                    "CHANGED_SL event returns OK code"{
                        setSLEvent(OrderEventType.CHANGED_SL)

                        val sellResult = runBrokerSell()

                        sellResult shouldBe BROKER_ADJUST_SL_OK
                    }

                    "CHANGE_REJECTED event returns failure code"{
                        setSLEvent(OrderEventType.CHANGE_REJECTED)

                        val sellResult = runBrokerSell()

                        sellResult shouldBe BROKER_ADJUST_SL_FAIL
                    }
                }*/
            }
        }
    }
}