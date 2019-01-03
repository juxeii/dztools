package com.jforex.dzjforex.history

import arrow.Kind
import com.jforex.dzjforex.history.BarFetchApi.fetchBars
import com.jforex.dzjforex.history.TickFetchApi.fetchTicks
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InstrumentApi.createInstrument
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUnixTimeFormat
import com.jforex.dzjforex.time.toUnixTime
import com.jforex.dzjforex.zorro.BROKER_HISTORY_UNAVAILABLE
import io.reactivex.Observable
import io.reactivex.rxkotlin.zipWith

data class FillParams<D>(
    val data: List<D>,
    val endCondition: (item: D) -> Boolean,
    val fillCall: (item: D, index: Int) -> Unit
)

object BrokerHistoryApi
{
    const val sizeOfT6Struct = 7

    fun <F> ContextDependencies<F>.brokerHistory(
        assetName: String,
        utcStartDate: Double,
        utcEndDate: Double,
        periodInMinutes: Int,
        noOfTicks: Int,
        out_TickInfoToFill: DoubleArray
    ): Kind<F, Int> =
        bindingCatch {
            if (!isConnected().bind()) BROKER_HISTORY_UNAVAILABLE
            else
            {
                val instrument = createInstrument(assetName).bind()
                val startTime = utcStartDate.toUnixTime()
                val endTime = utcEndDate.toUnixTime()

                logger.debug("Broker history called: " +
                        "instrument $instrument ," +
                        "startTime ${startTime.asUnixTimeFormat()} ,"+
                        "endTime ${endTime.asUnixTimeFormat()} ,"
                )

                if (periodInMinutes == 0)
                {
                    fetchTicks(
                        instrument,
                        startTime,
                        endTime,
                        noOfTicks,
                        out_TickInfoToFill
                    ).bind()
                } else
                {
                    fetchBars(
                        instrument,
                        startTime,
                        endTime,
                        periodInMinutes,
                        noOfTicks,
                        out_TickInfoToFill
                    ).bind()
                }
            }
        }.handleError {
            logger.debug("Fetching ticks failed with ${it.message}")
            BROKER_HISTORY_UNAVAILABLE
        }

    fun <F, D> ContextDependencies<F>.fillData(
        fillParams: FillParams<D>,
        noOfTicks: Int
    ): Kind<F, Int> = catch {
        val data = fillParams.data
        val noOfFilledTicks = Observable
            .fromIterable(data.asReversed())
            .zipWith(Observable.range(0, data.size))
            .takeUntil { fillParams.endCondition(it.first) || it.second == noOfTicks}
            .map { fillParams.fillCall(it.first, it.second)
                it.second
            }
            .blockingLast()+1
        logger.debug("Finally fetched $noOfTicks ticks")
        noOfFilledTicks
    }
}
