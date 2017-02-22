package com.jforex.dzjforex.time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;

import io.reactivex.Observable;

public class DateTimeUtils {

    private static final int DAYS_SINCE_UTC_EPOCH = 25569;
    private static SimpleDateFormat simpleUTCormat;
    private static final LocalDateTime ZERO_COM_TIME = LocalDateTime.of(1899, 12, 30, 0, 0);
    private static final BigDecimal MILLIS_PER_DAY = new BigDecimal(86400000);
    private final static Logger logger = LogManager.getLogger(DateTimeUtils.class);

    static {
        simpleUTCormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleUTCormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
    }

    private final IDataService dataService;

    public DateTimeUtils(final IDataService dataService) {
        this.dataService = dataService;
    }

    public boolean isMarketOffline(final long currentServerTime) {
        final Set<ITimeDomain> offlines = getOfflineTimes(currentServerTime,
                                                          currentServerTime + Period.ONE_MIN.getInterval());
        return offlines == null
                ? true
                : isServerTimeInOfflineDomains(currentServerTime, offlines);
    }

    public static double getOLEDateFromMillis(final long millis) {
        return DAYS_SINCE_UTC_EPOCH + (double) millis / (1000 * 3600 * 24);
    }

    public static double getOLEDateFromMillisRounded(final long millis) {
        return getOLEDateFromMillis(millis) + 1e-8;
    }

    public static LocalDateTime dateTimeFromOLEDate(final double oleTime) {
        final BigDecimal comTime = BigDecimal.valueOf(oleTime);
        final BigDecimal daysAfterZero = comTime.setScale(0, RoundingMode.DOWN);
        final BigDecimal fraction = comTime
            .subtract(daysAfterZero)
            .abs();
        final BigDecimal fractionMillisAfterZero = fraction
            .multiply(MILLIS_PER_DAY)
            .setScale(0, RoundingMode.HALF_DOWN);

        return ZERO_COM_TIME
            .plusDays(daysAfterZero.intValue())
            .plusNanos(millisToNano(fractionMillisAfterZero.intValue()));
    }

    public static long millisFromOLEDate(final double oleTime) {
        return dateTimeFromOLEDate(oleTime)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli();
    }

    public static long millisFromOLEDateRoundMinutes(final double oleTime) {
        return dateTimeFromOLEDate(oleTime)
            .toInstant(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MINUTES)
            .toEpochMilli();
    }

    private boolean isServerTimeInOfflineDomains(final long serverTime,
                                                 final Set<ITimeDomain> offlines) {
        for (final ITimeDomain offline : offlines)
            if (serverTime >= offline.getStart() && serverTime <= offline.getEnd())
                return true;
        return false;
    }

    private Set<ITimeDomain> getOfflineTimes(final long startTime,
                                             final long endTime) {
        return Observable
            .fromCallable(() -> dataService.getOfflineTimeDomains(startTime, endTime))
            .onErrorResumeNext(err -> {
                logger.error("Get market offline times  failed!" + err.getMessage());
                return Observable.just(new HashSet<>());
            })
            .blockingFirst();
    }

    public static String formatDateTime(final long dateTime) {
        return simpleUTCormat.format(new Date(dateTime));
    }

    public static String formatOLETime(final double oleTime) {
        final long dateTime = millisFromOLEDate(oleTime);
        return formatDateTime(dateTime);
    }

    public static Period getPeriodFromMinutes(final int minutes) {
        return Period.createCustomPeriod(Unit.Minute, minutes);
    }

    public static long getUTCYearStartTime(final int year) {
        return getUTCTime(year, 0, 1, 0, 0, 0);
    }

    public static long getUTCYearEndTime(final int year) {
        return getUTCTime(year, 11, 31, 23, 59, 0);
    }

    public static long getUTCTime(final int year,
                                  final int month,
                                  final int day,
                                  final int hour,
                                  final int min,
                                  final int sec) {
        final GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, min, sec);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar.getTimeInMillis();
    }

    public static double getUTCTimeFromBar(final IBar bar) {
        return getOLEDateFromMillisRounded(bar.getTime());
    }

    public static double getUTCTimeFromTick(final ITick tick) {
        return getOLEDateFromMillisRounded(tick.getTime());
    }

    public static long millisToNano(final long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }
}
