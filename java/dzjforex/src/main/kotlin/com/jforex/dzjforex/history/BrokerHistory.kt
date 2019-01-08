package com.jforex.dzjforex.history

import com.jforex.dzjforex.history.BarFetch.fetchBars
import com.jforex.dzjforex.history.TickFetch.fetchTicks
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.createInstrument
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUnixTimeFormat
import com.jforex.dzjforex.time.toUnixTime
import com.jforex.dzjforex.zorro.BROKER_HISTORY_UNAVAILABLE
import com.jforex.dzjforex.zorro.tickPeriod

object BrokerHistoryApi {
    fun <F> ContextDependencies<F>.brokerHistory(
        assetName: String,
        utcStartDate: Double,
        utcEndDate: Double,
        periodInMinutes: Int,
        noOfTicks: Int
    ) = bindingCatch {
        if (!isConnected().bind()) BrokerHistoryData(BROKER_HISTORY_UNAVAILABLE)
        else {
            val instrument = createInstrument(assetName).bind()
            val startTime = utcStartDate.toUnixTime()
            val endTime = utcEndDate.toUnixTime()
            logger.debug(
                "Fetching $noOfTicks ticks/bars for $instrument:\n" +
                        " startTime ${startTime.asUnixTimeFormat()}\n" +
                        " endTime ${endTime.asUnixTimeFormat()}\n" +
                        " periodInMinutes $periodInMinutes"
            )

            if (periodInMinutes == tickPeriod)
                fetchTicks(instrument, startTime, endTime, noOfTicks).bind()
            else
                fetchBars(instrument, startTime, endTime, periodInMinutes, noOfTicks).bind()
        }
    }.handleError { error ->
        logger.error(
            "BrokerHistory failed! Error message: " +
                    "${error.message} " +
                    "Stack trace: ${getStackTrace(error)}"
        )
        BrokerHistoryData(BROKER_HISTORY_UNAVAILABLE)
    }
}
