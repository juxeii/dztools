package com.jforex.dzjforex.history

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.BrokerHistoryApi.fillData
import com.jforex.dzjforex.history.BrokerHistoryApi.sizeOfT6Struct
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.kforexutils.price.Price

object TickFetchApi
{
    fun <F> ContextDependencies<F>.fetchTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        noOfTicks: Int,
        out_TickInfoToFill: DoubleArray
    ): Kind<F, Int> =
        bindingCatch {
            val endTickTime = getLatesTickTime(instrument, endTime).bind()
            val fetchedTicks = getTicks(instrument, startTime, endTickTime).bind()

            val endCondition: (tick: ITick) -> Boolean = { it.time < startTime }
            val fillCall: (tick: ITick, index: Int) -> Unit = { tick, index ->
                fillTickInfo(tick, instrument, out_TickInfoToFill, index)
            }
            val fillParams = FillParams(fetchedTicks, endCondition, fillCall)
            fillData(fillParams, noOfTicks).bind()
        }

    fun <F> ContextDependencies<F>.getLatesTickTime(instrument: Instrument, endTime: Long): Kind<F, Long> =
        catch { minOf(history.getTimeOfLastTick(instrument), endTime) }

    fun <F> ContextDependencies<F>.getTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long
    ): Kind<F, List<ITick>> = catch { history.getTicks(instrument, startTime, endTime) }

    fun fillTickInfo(
        tick: ITick,
        instrument: Instrument,
        out_TickInfoToFill: DoubleArray,
        tickIndex: Int
    )
    {
        val startIndex = tickIndex * sizeOfT6Struct
        val ask = tick.ask
        val utcTime = tick.time.toUTCTime()
        val spread = Price(instrument, tick.bid - tick.ask).toDouble()
        val volume = tick.askVolume

        out_TickInfoToFill[startIndex] = ask;
        out_TickInfoToFill[startIndex + 1] = ask;
        out_TickInfoToFill[startIndex + 2] = ask;
        out_TickInfoToFill[startIndex + 3] = ask;
        out_TickInfoToFill[startIndex + 4] = utcTime;
        out_TickInfoToFill[startIndex + 5] = spread;
        out_TickInfoToFill[startIndex + 6] = volume;

        /*logger.debug(
            "Stored tick for " + instrument
                    + " ask " + ask
                    + " time " + formatUTCTime(utcTime)
                    + " spread " + spread
                    + " volume " + volume
        )*/
    }
}
