package com.jforex.dzjforex.test.util;

import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;

public class TestSchedulerRule implements TestRule {

    private final TestScheduler testScheduler = new TestScheduler();

    public void advanceTimeBy(final long delayTime, final TimeUnit unit) {
        testScheduler.advanceTimeBy(delayTime, unit);
    }

    public void advanceTimeInMillisBy(final long delayTime) {
        advanceTimeBy(delayTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                RxJavaPlugins.setIoSchedulerHandler(scheduler -> testScheduler);
                RxJavaPlugins.setComputationSchedulerHandler(scheduler -> testScheduler);
                RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> testScheduler);

                try {
                    base.evaluate();
                } finally {
                    RxJavaPlugins.reset();
                }
            }
        };
    }
}