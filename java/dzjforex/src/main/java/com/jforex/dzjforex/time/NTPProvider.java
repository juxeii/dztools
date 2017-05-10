package com.jforex.dzjforex.time;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class NTPProvider {

    private final NTPFetch ntpFetch;
    private final PluginConfig pluginConfig;
    private final BehaviorSubject<Long> latestNTPTime = BehaviorSubject.create();

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
        latestNTPTime.onNext(ntpTime);
    }

    public long get() {
        return latestNTPTime.getValue();
    }
}
