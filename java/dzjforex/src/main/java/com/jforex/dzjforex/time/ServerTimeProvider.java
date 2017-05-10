package com.jforex.dzjforex.time;

import java.time.Clock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.reactivex.Single;

public class ServerTimeProvider {

    private final NTPProvider ntpProvider;
    private final TickTimeProvider tickTimeProvider;
    private final Clock clock;
    private long latestNTP;
    private long synchTime;

    private final static Logger logger = LogManager.getLogger(ServerTimeProvider.class);

    public ServerTimeProvider(final NTPProvider ntpProvider,
                              final TickTimeProvider tickTimeProvider,
                              final Clock clock) {
        this.ntpProvider = ntpProvider;
        this.tickTimeProvider = tickTimeProvider;
        this.clock = clock;
    }

    public Single<Long> get() {
        return ntpProvider
            .get()
            .flatMap(this::serverTimeFromValidNTP)
            .onErrorResumeNext(serverTimeFromTick());
    }

    private Single<Long> serverTimeFromTick() {
        return Single.defer(() -> {
            logger.warn("Currently no NTP available, estimating with latest tick time...");
            return tickTimeProvider.get();
        });
    }

    private Single<Long> serverTimeFromValidNTP(final long ntpFromProvider) {
        if (ntpFromProvider > latestNTP) {
            storeLatestNTP(ntpFromProvider);
            return Single.just(ntpFromProvider);
        }
        final long timeDiffToSynchTime = clock.millis() - synchTime;
        return Single.just(latestNTP + timeDiffToSynchTime);
    }

    private void storeLatestNTP(final long newNTP) {
        latestNTP = newNTP;
        synchTime = clock.millis();
    }
}
