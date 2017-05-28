package com.jforex.dzjforex.brokertime.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.NTPFetch;
import com.jforex.dzjforex.brokertime.NTPSynchTask;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class NTPSynchTaskTest extends CommonUtilForTest {

    private NTPSynchTask ntpSynchTask;

    @Mock
    private NTPFetch ntpFetchMock;
    private final long ntpSynchInterval = 500L;

    @Before
    public void setUp() {
        when(pluginConfigMock.ntpSynchInterval()).thenReturn(ntpSynchInterval);

        ntpSynchTask = new NTPSynchTask(ntpFetchMock, pluginConfigMock);
    }

    private TestObserver<Long> subscribe() {
        final TestObserver<Long> observer = ntpSynchTask
            .get()
            .test();
        rxTestScheduler.advanceTimeInMillisBy(1L);
        return observer;
    }

    private OngoingStubbing<Single<Long>> stubNTPFetch() {
        return when(ntpFetchMock.get());
    }

    @Test
    public void getCallIsDeferred() {
        ntpSynchTask.get();

        verifyZeroInteractions(ntpFetchMock);
    }

    @Test
    public void whenNTPFetchFailsErrorIsPropagated() {
        stubNTPFetch().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenNTPFetchSucceeds {

        private final long ntp = 42L;
        private final long secondNTP = 48L;

        @Before
        public void setUp() {
            stubNTPFetch()
                .thenReturn(Single.just(ntp))
                .thenReturn(Single.just(secondNTP));
        }

        @Test
        public void ntpIsReturnedAndTaskIsStillRunning() {
            subscribe()
                .assertValue(ntp)
                .assertNotComplete();
        }

        @Test
        public void secondNTPIsReturnedAfterSynchInterval() {
            final TestObserver<Long> observer = subscribe();

            rxTestScheduler.advanceTimeInMillisBy(ntpSynchInterval);

            observer
                .assertValues(ntp, secondNTP)
                .assertNotComplete();
        }
    }
}
