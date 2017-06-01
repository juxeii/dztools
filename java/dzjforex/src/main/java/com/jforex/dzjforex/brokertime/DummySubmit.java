package com.jforex.dzjforex.brokertime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.subjects.BehaviorSubject;

public class DummySubmit {

    private final DummySubmitRunner dummySubmitRunner;
    private final BehaviorSubject<Long> submitTime = BehaviorSubject.createDefault(0L);

    private final static int minuteForHour = 30;
    private final static long minDiffToNextSubmit = 60000;
    private final static Logger logger = LogManager.getLogger(DummySubmit.class);

    public DummySubmit(final DummySubmitRunner dummySubmitRunner) {
        this.dummySubmitRunner = dummySubmitRunner;
    }

    public boolean wasOffline(final long serverTime) {
        checkForSubmit(serverTime);
        return dummySubmitRunner.wasOffline();
    }

    private void checkForSubmit(final long serverTime) {
        final int serverMinute = (int) ((serverTime / (1000 * 60)) % 60);
        final boolean isMinuteForCheck = serverMinute % minuteForHour == 0;
        logger.debug("serverTime " + DateTimeUtil.formatMillis(serverTime)
                + " serverMinute " + serverMinute
                + " isMinuteForCheck " + isMinuteForCheck);
        if (!isMinuteForCheck) {
            logger.debug("No time for dummy submit");
            return;
        }

        final long diffToLatestSubmit = serverTime - submitTime.getValue();
        logger.debug("diffToLatestSubmit " + diffToLatestSubmit);
        if (diffToLatestSubmit <= minDiffToNextSubmit) {
            logger.debug("diffToLatestSubmit smaller than minDiffToNextSubmit");
            return;
        }

        submitTime.onNext(serverTime);
        dummySubmitRunner.start();
    }
}
