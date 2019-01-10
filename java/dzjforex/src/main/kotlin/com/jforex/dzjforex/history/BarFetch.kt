package com.jforex.dzjforex.history

import com.dukascopy.api.*
import com.dukascopy.api.Unit
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUnixTimeFormat
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.kforexutils.history.retry
import io.reactivex.Observable

object BarFetch {
    fun <F> ContextDependencies<F>.fetchBars(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        periodInMinutes: Int,
        numberOfBars: Int
    ) = bindingCatch {
        val period = createPeriod(periodInMinutes).bind()
        val endBarTime = getBarEndTime(
            instrument = instrument,
            period = period,
            endTime = endTime
        ).bind()
        val startBarTime = getBarStartTime(
            period = period,
            endBarTime = endBarTime,
            rawStartBarTime = startTime,
            numberOfBars = numberOfBars
        ).bind()
        val fetchedBars = getBars(
            instrument = instrument,
            period = period,
            from = startBarTime,
            to = endBarTime
        ).bind()
        BrokerHistoryData(fetchedBars.size, fetchedBars)
    }

    fun <F> ContextDependencies<F>.createPeriod(barMinutes: Int) = catch {
        Period.createCustomPeriod(Unit.Minute, barMinutes)
    }

    fun <F> ContextDependencies<F>.getBarEndTime(
        instrument: Instrument,
        period: Period,
        endTime: Long
    ) = catch {
        val latestBarTime = history.retry { getBar(instrument, period, OfferSide.ASK, 1).time }
        val barStartForEndTime = history.retry { getBarStart(period, endTime) }
        minOf(latestBarTime, barStartForEndTime)
    }

    fun <F> ContextDependencies<F>.getBarStartTime(
        period: Period,
        endBarTime: Long,
        rawStartBarTime: Long,
        numberOfBars: Int
    ) = delay {
        val startTimeForNBarsBack = history.retry { getTimeForNBarsBack(period, endBarTime, numberOfBars) }
        val startTimeForZorroStartTime = history.retry { getBarStart(period, rawStartBarTime) }
        val startTimeAdapted =
            if (startTimeForZorroStartTime < rawStartBarTime) startTimeForZorroStartTime + period.getInterval()
            else startTimeForZorroStartTime
        maxOf(startTimeAdapted, startTimeForNBarsBack)
    }

    fun <F> ContextDependencies<F>.getBars(
        instrument: Instrument,
        period: Period,
        from: Long,
        to: Long
    ) = delay {
        Observable
            .fromIterable(
                history.retry { getBars(instrument, period, OfferSide.ASK, Filter.NO_FILTER, from, to).asReversed() }
            )
            .map { bar ->
                logger.debug("Stored Bar time ${bar.time.asUnixTimeFormat()}")
                createT6Data(bar, period)
            }
            .toList()
            .blockingGet()
    }

    fun createT6Data(bar: IBar, period: Period) = T6Data(
        time = (bar.time + period.getInterval()).toUTCTime(),
        high = bar.high.toFloat(),
        low = bar.low.toFloat(),
        open = bar.open.toFloat(),
        close = bar.close.toFloat(),
        value = 0.0F,
        volume = bar.volume.toFloat()
    )
}
