package com.jforex.dzjforex.history

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.BrokerHistoryApi.fillData
import com.jforex.dzjforex.history.BrokerHistoryApi.sizeOfT6Struct
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUnixTimeFormat
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.dzjforex.zorro.BROKER_HISTORY_UNAVAILABLE
import com.jforex.kforexutils.price.Price
import io.reactivex.Observable
import io.reactivex.Single


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
            logger.debug("fetchTicks endTickTime ${endTickTime.asUnixTimeFormat()}")
            val fetchedTicks = getTicksWithShift(instrument, endTickTime, noOfTicks)
            logger.debug("fetchTicks size ${fetchedTicks.size}")

            val endCondition: (tick: ITick) -> Boolean = { it.time < startTime }
            val fillCall: (tick: ITick, index: Int) -> Unit = { tick, index ->
                fillTickInfo(tick, instrument, out_TickInfoToFill, index)
            }
            val fillParams = FillParams(fetchedTicks, endCondition, fillCall)
            fillData(fillParams, noOfTicks).bind()
        }.handleError { error ->
            logger.error("FetchTicks error! ${error.message} Stack trace: ${getStackTrace(error)}")
            BROKER_HISTORY_UNAVAILABLE
        }

    fun <F> ContextDependencies<F>.getLatesTickTime(instrument: Instrument, endTime: Long): Kind<F, Long> =
        catch { minOf(history.getTimeOfLastTick(instrument), endTime) }

    fun <F> ContextDependencies<F>.getTicksWithShift(
        instrument: Instrument,
        endTime: Long,
        shift: Int
    ): List<ITick>
    {
        return Observable
            .defer { startDates(instrument, endTime) }
            .map { startDate ->
                history.getTicks(instrument, startDate, startDate + pluginSettings.tickfetchmillis() - 1)
            }
            .concatMapIterable { it }
            .take(shift.toLong())
            .toList()
            .blockingGet()
    }

    fun <F> ContextDependencies<F>.startDates(instrument: Instrument, endTime: Long) =
        Single
            .just(endTime)
            .flatMapObservable { countStreamForTickFetch(it) }

    fun <F> ContextDependencies<F>.countStreamForTickFetch(endTime: Long): Observable<Long>
    {
        val seq = generateSequence(0) { it + 1 }.map { counter ->
            endTime - counter * pluginSettings.tickfetchmillis() + 1
        }
        return Observable.fromIterable(seq.asIterable())
    }

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
