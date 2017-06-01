package com.jforex.dzjforex.brokertime.ntp.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokertime.ntp.NTPProvider;
import com.jforex.dzjforex.brokertime.ntp.NTPSynchTask;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class NTPProviderTest extends CommonUtilForTest {

    private NTPProvider ntpProvider;

    @Mock
    private NTPSynchTask ntpSynchTaskMock;
    private final Subject<Long> synchTaskSubject = PublishSubject.create();

    private final long ntpRetryDelay = 5000L;
    private final long ntp = 12L;
    private final long watchTime = 14L;

    @Before
    public void setUp() {
        setUpMocks();

        ntpProvider = new NTPProvider(ntpSynchTaskMock,
                                      timeWatchMock,
                                      pluginConfigMock);
    }

    private void setUpMocks() {
        when(pluginConfigMock.ntpRetryDelay()).thenReturn(ntpRetryDelay);

        when(ntpSynchTaskMock.get()).thenReturn(synchTaskSubject);

        when(timeWatchMock.getForNewTime(ntp)).thenReturn(watchTime);
    }

    private TestObserver<Long> subscribe() {
        final TestObserver<Long> observer = ntpProvider
            .get()
            .test();
        rxTestScheduler.advanceTimeInMillisBy(1L);
        return observer;
    }

    @Test
    public void synchTaskMockIsSubscribedAfterSUTCreation() {
        verify(ntpSynchTaskMock).get();
    }

    @Test
    public void errorIsPropagatedWhenNoNTPWasReceivedYet() {
        subscribe().assertError(Exception.class);
    }

    @Test
    public void whenSnychTaskFailsRetryIsPerformed() {
        synchTaskSubject.onError(jfException);

        rxTestScheduler.advanceTimeInMillisBy(ntpRetryDelay);

        verify(ntpSynchTaskMock, times(2)).get();
    }

    @Test
    public void whenSnychTaskSucceedsValueFromTimeWatchIsReturned() {
        synchTaskSubject.onNext(ntp);

        subscribe()
            .assertValue(watchTime)
            .assertComplete();
    }
}