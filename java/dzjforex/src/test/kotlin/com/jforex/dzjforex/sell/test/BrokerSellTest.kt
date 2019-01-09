package com.jforex.dzjforex.sell.test

import arrow.core.Option
import arrow.core.toOption
import arrow.effects.fix
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.sell.BrokerSellApi.brokerSell
import com.jforex.dzjforex.zorro.BROKER_SELL_FAIL
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.close
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Observable

class BrokerSellTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val testOrder = mockk<IOrder>()
    private val testInstrument = mockk<Instrument>()
    private val orderId = 123
    private val contracts = 12300
    private val amount = 0.0123
    private val slippage = 6.0
    private val limitPrice = 1.12345
    private val maybeLlimitPrice = limitPrice.toOption()

    private fun runBrokerSell(maybeLimitPrice: Option<Double>) = contextApi
        .brokerSell(orderId = orderId, contracts = contracts, maybeLimitPrice = maybeLimitPrice, slippage = slippage)
        .fix()
        .unsafeRunSync()

    init {
        every { testOrder.instrument } returns testInstrument
        every { testOrder.id } returns (orderId.toString())
        every { testInstrument.pipScale } returns 4
        every { testInstrument.pipValue } returns 0.08

        "BrokerSell returns failing code when Id not found" {
            every { contextApi.engine.orders } returns (emptyList())
            every { contextApi.history.getHistoricalOrderById(orderId.toString()) } returns (null)

            val sellResult = runBrokerSell(maybeLlimitPrice)

            sellResult shouldBe BROKER_SELL_FAIL
        }

        "When order Id is found" - {
            every { contextApi.engine.orders } returns (listOf(testOrder))

            "BrokerSell returns failing code when instrument is not tradeable" {
                every { testInstrument.isTradable } returns false

                val sellResult = runBrokerSell(maybeLlimitPrice)

                sellResult shouldBe BROKER_SELL_FAIL
            }

            "When order instrument is tradeable" - {
                every { testInstrument.isTradable } returns true
                mockkStatic("com.jforex.kforexutils.order.extension.OrderCloseExtensionKt")

                "When order is already closed order id is returned"{
                    every { testOrder.state} returns IOrder.State.CLOSED

                    val sellResult = runBrokerSell(maybeLlimitPrice)

                    sellResult shouldBe orderId
                }

                "When close call fails failing code is returned"{
                    every { testOrder.state} returns IOrder.State.FILLED
                    every { testOrder.close(any(), any(), any(), any()) } throws JFException("Test exception")

                    val sellResult = runBrokerSell(maybeLlimitPrice)

                    sellResult shouldBe BROKER_SELL_FAIL
                }

                "When close call succeeds" - {
                    every { testOrder.state} returns IOrder.State.FILLED

                    fun setCloseEvent(type: OrderEventType) {
                        val orderEvent = OrderEvent(testOrder, type)
                        every { testOrder.close(amount, any(), slippage, any()) } returns Observable.just(orderEvent)
                    }

                    "Close call parameters are correct"{
                        setCloseEvent(OrderEventType.CLOSE_OK)

                        runBrokerSell(maybeLlimitPrice)

                        verify { testOrder.close(amount, limitPrice, slippage, any()) }
                    }

                    "CLOSE_OK event returns order id"{
                        setCloseEvent(OrderEventType.CLOSE_OK)

                        val sellResult = runBrokerSell(maybeLlimitPrice)

                        sellResult shouldBe orderId
                    }

                    "PARTIAL_CLOSE_OK event returns order id"{
                        setCloseEvent(OrderEventType.PARTIAL_CLOSE_OK)

                        val sellResult = runBrokerSell(maybeLlimitPrice)

                        sellResult shouldBe orderId
                    }

                    "No limit price result in default preferred close price"{
                        setCloseEvent(OrderEventType.CLOSE_OK)

                        runBrokerSell(0.0.toOption())

                        verify { testOrder.close(amount, 0.0, slippage, any()) }
                    }
                }
            }
        }
    }
}