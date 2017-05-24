package com.jforex.dzjforex.brokertime;

import java.util.concurrent.TimeUnit;

import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

public class NTPProvider {

    private final NTPSynchTask ntpSynchTask;
    private final TimeWatch timeWatch;
    private final long noNTPAvailable = 0L;
    private final BehaviorSubject<Long> latestNTPTime = BehaviorSubject.createDefault(noNTPAvailable);

    public NTPProvider(final NTPSynchTask ntpSynchTask,
                       final TimeWatch timeWatch,
                       final PluginConfig pluginConfig) {
        this.ntpSynchTask = ntpSynchTask;
        this.timeWatch = timeWatch;

        subscribeToNTPSynchTask(pluginConfig.ntpRetryDelay());
    }

    private void subscribeToNTPSynchTask(final long retryDelay) {
        Observable
            .defer(() -> ntpSynchTask.get())
            .retryWhen(errors -> errors.flatMap(e -> Observable.timer(retryDelay, TimeUnit.MILLISECONDS)))
            .subscribe(this::processNewNTP);
    }

    private void processNewNTP(final long newNTP) {
        latestNTPTime.onNext(newNTP);
        if (newNTP > timeWatch.get())
            timeWatch.synch(newNTP);
    }

    public Single<Long> get() {
        return latestNTPTime.getValue() == noNTPAvailable
                ? Single.error(new JFException("No NTP available yet."))
                : Single.just(timeWatch.get());
    }
}