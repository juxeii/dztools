package com.jforex.dzjforex.brokertime;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class NTPSynchTask {

    private final NTPFetch ntpFetch;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(NTPSynchTask.class);

    public NTPSynchTask(final NTPFetch ntpFetch,
                        final PluginConfig pluginConfig) {
        this.ntpFetch = ntpFetch;
        this.pluginConfig = pluginConfig;
    }

    public Observable<Long> get() {
        return Observable
            .interval(0L,
                      pluginConfig.ntpSynchInterval(),
                      TimeUnit.MILLISECONDS,
                      Schedulers.io())
            .doOnNext(counter -> logger.debug("Starting NTP synch task..."))
            .flatMapSingle(counter -> ntpFetch.get())
            .doOnNext(ntp -> logger.error("Received new NTP " + DateTimeUtil.formatMillis(ntp)))
            .doOnError(e -> logger.error("NTP synch failed! " + e.getMessage()));
    }
}
