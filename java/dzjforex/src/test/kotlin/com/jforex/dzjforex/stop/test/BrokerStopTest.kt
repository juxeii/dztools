package com.jforex.dzjforex.stop.test

import arrow.effects.fix
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.mock.test.getContextDependenciesForTest_IO
import com.jforex.dzjforex.stop.BrokerStopApi.brokerStop
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_FAIL
import com.jforex.dzjforex.zorro.BROKER_ADJUST_SL_OK
import com.jforex.kforexutils.order.event.OrderEvent
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.order.extension.setSL
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Observable

class BrokerStopTest : FreeSpec() {

    private val contextApi = getContextDependenciesForTest_IO()
    private val testOrder = mockk<IOrder>()
    private val testInstrument = mockk<Instrument>()
    private val orderId = 123
    private val slPrice = 1.13456

    private fun runBrokerStop() = contextApi
        .brokerStop(orderId, slPrice)
        .fix()
        .unsafeRunSync()

    init {
        every { testOrder.instrument } returns testInstrument
        every { testOrder.id } returns (orderId.toString())

        "BrokerStop returns failing code when Id not found" {
            every { contextApi.engine.orders } returns (emptyList())
            every { contextApi.history.getHistoricalOrderById(orderId.toString()) } returns (null)

            val stopResult = runBrokerStop()

            stopResult shouldBe BROKER_ADJUST_SL_FAIL
        }

        "When order Id is found" - {
            every { contextApi.engine.orders } returns (listOf(testOrder))

            "BrokerStop returns failing code when instrument is not tradeable" {
                every { testInstrument.isTradable } returns (false)

                val stopResult = runBrokerStop()

                stopResult shouldBe BROKER_ADJUST_SL_FAIL
            }

            "When order instrument is tradeable" - {
                every { testInstrument.isTradable } returns (true)
                mockkStatic("com.jforex.kforexutils.order.extension.OrderSetSLExtensionKt")

                "When stop loss call fails failing code is returned"{
                    every { testOrder.setSL(any(), any(), any(), any()) } throws JFException("Test exception")

                    val stopResult = runBrokerStop()

                    stopResult shouldBe BROKER_ADJUST_SL_FAIL
                }

                "When stop loss call succeeds" - {
                    fun setSLEvent(type: OrderEventType) {
                        val orderEvent = OrderEvent(testOrder, type)
                        every { testOrder.setSL(slPrice, any(), any(), any()) } returns Observable.just(orderEvent)
                    }

                    "CHANGED_SL event returns OK code"{
                        setSLEvent(OrderEventType.CHANGED_SL)

                        val stopResult = runBrokerStop()

                        stopResult shouldBe BROKER_ADJUST_SL_OK
                    }

                    "CHANGE_REJECTED event returns failure code"{
                        setSLEvent(OrderEventType.CHANGE_REJECTED)

                        val stopResult = runBrokerStop()

                        stopResult shouldBe BROKER_ADJUST_SL_FAIL
                    }
                }
            }
        }
    }
}