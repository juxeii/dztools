package com.jforex.dzjforex.time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.DateTimeUtil;

public class ServerTimeProvider {

    private final NTPProvider ntpProvider;
    private final TickTimeProvider tickTimeProvider;
    private long latestNTP;
    private long synchTime;

    private final static Logger logger = LogManager.getLogger(ServerTimeProvider.class);

    public ServerTimeProvider(final NTPProvider ntpProvider,
                              final TickTimeProvider tickTimeProvider) {
        this.ntpProvider = ntpProvider;
        this.tickTimeProvider = tickTimeProvider;
    }

    public long get() {
        final long ntpFromProvider = ntpProvider.get();
        return ntpFromProvider == 0L
                ? serverTimeFromTick()
                : serverTimeFromNTPProvider(ntpFromProvider);
    }

    private long serverTimeFromTick() {
        logger.info("Currently no NTP available, estimating with latest tick time.");
        return tickTimeProvider.get();
    }

    private long serverTimeFromNTPProvider(final long ntpFromProvider) {
        return ntpFromProvider > latestNTP
                ? fromNewNTP(ntpFromProvider)
                : fromOldNTP();
    }

    private long fromNewNTP(final long newNTP) {
        latestNTP = newNTP;
        synchTime = DateTimeUtil.localMillisNow();
        return newNTP;
    }

    private long fromOldNTP() {
        final long timeDiffToSynchTime = DateTimeUtil.localMillisNow() - synchTime;
        return latestNTP + timeDiffToSynchTime;
    }
}
