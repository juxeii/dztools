package com.jforex.dzjforex.time;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class NTPProvider {

    private final NTPFetch ntpFetch;
    private final PluginConfig pluginConfig;
    private long latestNTPTime;

    private final static Logger logger = LogManager.getLogger(NTPProvider.class);

    public NTPProvider(final NTPFetch ntpFetch,
                       final PluginConfig pluginConfig) {
        this.ntpFetch = ntpFetch;
        this.pluginConfig = pluginConfig;

        startNTPSynchTask();
    }

    private void startNTPSynchTask() {
        Observable
            .interval(0L,
                      pluginConfig.ntpSynchInterval(),
                      TimeUnit.MILLISECONDS,
                      Schedulers.io())
            .doOnSubscribe(d -> logger.debug("Starting NTP synch task..."))
            .flatMapSingle(counter -> ntpFetch.get())
            .subscribe(this::onNTPTime,
                       e -> logger.debug("NTP synchronization task failed! " + e.getMessage()));
    }

    private void onNTPTime(final long ntpTime) {
        logger.debug("New NTP received " + DateTimeUtil.formatMillis(ntpTime));
        latestNTPTime = ntpTime;
    }

    public Single<Long> get() {
        return latestNTPTime == 0L
                ? Single.error(new JFException("No NTP available yet."))
                : Single.just(latestNTPTime);
    }
}
