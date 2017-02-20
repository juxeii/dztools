package com.jforex.dzjforex.time;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;

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
                      TimeUnit.MILLISECONDS)
            .doOnSubscribe(d -> logger.debug("Starting NTP synch task..."))
            .flatMap(counter -> ntpFetch.observable())
            .subscribe(this::onNTPTime,
                       err -> logger.debug("NTP synchronization task failed with error: " + err.getMessage()));
    }

    private void onNTPTime(final long ntpTime) {
        logger.debug("New NTP received " + DateTimeUtil.formatMillis(ntpTime));
        latestNTPTime = ntpTime;
    }

    public long get() {
        return latestNTPTime;
    }
}
