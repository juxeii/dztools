package com.jforex.dzjforex.brokertime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.subjects.BehaviorSubject;

public class DummySubmit {

    private final DummySubmitRunner dummySubmitRunner;
    private final BehaviorSubject<Long> submitTime = BehaviorSubject.createDefault(0L);

    private final int minuteForHour;
    private final static Logger logger = LogManager.getLogger(DummySubmit.class);

    public DummySubmit(final DummySubmitRunner dummySubmitRunner,
                       final PluginConfig pluginConfig) {
        this.dummySubmitRunner = dummySubmitRunner;

        minuteForHour = pluginConfig.dummySubmitMinuteForHour();
    }

    public boolean wasOffline(final long serverTime) {
        checkForSubmit(serverTime);
        return dummySubmitRunner.wasOffline();
    }

    private void checkForSubmit(final long serverTime) {
        final int serverMinute = serverMinute(serverTime);
        final boolean isMinuteForCheck = serverMinute % minuteForHour == 0;
        if (!isMinuteForCheck)
            return;

        final int submitServerMinute = serverMinute(submitTime.getValue());
        if (submitServerMinute == serverMinute)
            return;

        logger.debug("Starting next dummy submit. ServerTime " + DateTimeUtil.formatMillis(serverTime)
                + " serverMinute " + serverMinute
                + " submitServerMinute " + submitServerMinute
                + " isMinuteForCheck " + isMinuteForCheck);
        submitTime.onNext(serverTime);
        dummySubmitRunner.start();
    }

    private int serverMinute(final long serverTime) {
        return (int) ((serverTime / (1000 * 60)) % 60);
    }
}
