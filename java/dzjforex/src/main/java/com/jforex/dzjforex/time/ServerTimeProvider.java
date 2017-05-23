package com.jforex.dzjforex.time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.reactivex.Single;

public class ServerTimeProvider {

    private final NTPProvider ntpProvider;
    private final TickTimeProvider tickTimeProvider;
    private final TimeWatch timeWatch;

    private final static Logger logger = LogManager.getLogger(ServerTimeProvider.class);

    public ServerTimeProvider(final NTPProvider ntpProvider,
                              final TickTimeProvider tickTimeProvider,
                              final TimeWatch timeWatch) {
        this.ntpProvider = ntpProvider;
        this.tickTimeProvider = tickTimeProvider;
        this.timeWatch = timeWatch;
    }

    public Single<Long> get() {
        return Single.defer(() -> ntpProvider
            .get()
            .map(this::serverTimeFromValidNTP)
            .onErrorResumeNext(serverTimeFromTick()));
    }

    private Single<Long> serverTimeFromTick() {
        return Single.defer(() -> {
            logger.warn("Currently no NTP available, estimating with latest tick time...");
            return tickTimeProvider.get();
        });
    }

    private long serverTimeFromValidNTP(final long latestNTP) {
        final long currentTimeWatch = timeWatch.get();
        if (latestNTP > currentTimeWatch) {
            timeWatch.synch(latestNTP);
            return latestNTP;
        }
        return currentTimeWatch;
    }
}
