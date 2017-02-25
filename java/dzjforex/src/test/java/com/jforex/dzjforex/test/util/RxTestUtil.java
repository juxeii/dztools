package com.jforex.dzjforex.test.util;

import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;

public final class RxTestUtil {

    private static final RxTestUtil instance = new RxTestUtil();
    private static final TestScheduler testScheduler = new TestScheduler();
    private static final Scheduler immediate = new Scheduler() {
        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(Runnable::run);
        }
    };

    public RxTestUtil() {
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> testScheduler);
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> immediate);
    }

    public static final RxTestUtil get() {
        return instance;
    }

    public static final void advanceTimeBy(final long delayTime,
                                           final TimeUnit timeUnit) {
        testScheduler.advanceTimeBy(delayTime, timeUnit);
    }

    public static final void advanceTimeInMillisBy(final long delayTime) {
        advanceTimeBy(delayTime, TimeUnit.MILLISECONDS);
    }
}
