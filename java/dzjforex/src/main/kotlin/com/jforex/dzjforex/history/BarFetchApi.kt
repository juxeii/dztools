package com.jforex.dzjforex.history

import arrow.Kind
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.*
import com.dukascopy.api.Unit
import com.dukascopy.charts.data.datacache.DataCacheUtils
import com.jforex.dzjforex.history.BrokerHistoryApi.fillData
import com.jforex.dzjforex.history.BrokerHistoryApi.sizeOfT6Struct
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUnixTimeFormat
import com.jforex.dzjforex.time.toUTCTime

object BarFetchApi
{
    fun <F> ContextDependencies<F>.fetchBars(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        periodInMinutes: Int,
        noOfTicks: Int,
        out_TickInfoToFill: DoubleArray
    ): Kind<F, Int> =
        bindingCatch {
            val period = createPeriod(periodInMinutes).bind()
            val endBarTime = getLatesBarTime(instrument, period, endTime).bind()
            val startBarTime = getBarTimeForStartTime(period, startTime).bind()
            val fetchedBars = getBars(instrument, period, noOfTicks, endBarTime).bind()

            logger.debug(
                "Broker history for bars called: " +
                        "period $period ," +
                        "noOfTicks $noOfTicks ," +
                        "startBarTime ${startBarTime.asUnixTimeFormat()} ," +
                        "endBarTime ${endBarTime.asUnixTimeFormat()} ," +
                        //"fetchedBars $fetchedBars"+
                        "fetched ${fetchedBars.size} bars"
            )
            val endCondition: (bar: IBar) -> Boolean = { it.time < startBarTime }
            val fillCall: (bar: IBar, index: Int) -> kotlin.Unit = { bar, index ->
                fillBarInfo(bar, instrument, out_TickInfoToFill, index)
            }
            val fillParams = FillParams(fetchedBars, endCondition, fillCall)
            fillData(fillParams, noOfTicks).bind()
        }

    fun <F> ContextDependencies<F>.createPeriod(barMinutes: Int) =
        catch { Period.createCustomPeriod(Unit.Minute, barMinutes) }

    fun <F> ContextDependencies<F>.getLatesBarTime(
        instrument: Instrument,
        period: Period,
        endTime: Long
    ): Kind<F, Long> = catch {
        val latestBarTime = history.getBar(instrument, period, OfferSide.ASK, 1).time
        val barStartForEndTime = DataCacheUtils.getCandleStartFast(period, endTime)
        logger.debug(
            "getLatesBarTime called: " +
                    " endTime ${endTime.asUnixTimeFormat()} ," +
                    " latestBarTime ${latestBarTime.asUnixTimeFormat()} ," +
                    " barStartForEndTime ${barStartForEndTime.asUnixTimeFormat()}"
        )
        minOf(latestBarTime, barStartForEndTime)
    }

    fun <F> ContextDependencies<F>.getBarTimeForStartTime(period: Period, startTime: Long): Kind<F, Long> =
        catch { DataCacheUtils.getCandleStartFast(period, startTime) }

    fun <F> ContextDependencies<F>.getBars(
        instrument: Instrument,
        period: Period,
        noOfBars: Int,
        endTime: Long
    ): Kind<F, List<IBar>> = catch {
        history.getBars(
            instrument,
            period,
            OfferSide.ASK,
            Filter.NO_FILTER,
            noOfBars,
            endTime,
            0
        )
    }

    fun fillBarInfo(
        bar: IBar,
        instrument: Instrument,
        out_TickInfoToFill: DoubleArray,
        barIndex: Int
    )
    {
        val startIndex = barIndex * sizeOfT6Struct
        val open = bar.open
        val close = bar.close
        val high = bar.high
        val low = bar.low
        val utcTime:Double = bar.time.toUTCTime()
        val spread = 0.0
        val volume = bar.volume

        out_TickInfoToFill[startIndex] = open;
        out_TickInfoToFill[startIndex + 1] = close;
        out_TickInfoToFill[startIndex + 2] = high;
        out_TickInfoToFill[startIndex + 3] = low;
        out_TickInfoToFill[startIndex + 4] = utcTime;
        out_TickInfoToFill[startIndex + 5] = spread;
        out_TickInfoToFill[startIndex + 6] = volume;

        /*logger.debug(
            "Stored bar for $instrument: "
                    + " open " + open
                    + " close " + close
                    + " high " + high
                    + " low " + low
                    + " bartime " + formatUnixTime(bar.time)
                    + " time " + formatUTCTime(utcTime)
                    + " spread " + spread
                    + " volume " + volume
        );*/
    }
}
