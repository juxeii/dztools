package com.jforex.dzjforex.brokertime.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.TickTimeProvider;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.dzjforex.brokertime.TickTimeFetch;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class TickTimeProviderTest extends CommonUtilForTest {

    private TickTimeProvider tickTimeProvider;

    @Mock
    private TickTimeFetch tickTimeRepositoryMock;
    private final long tickTime = 12L;
    private final long watchTime = 14L;

    @Before
    public void setUp() {
        tickTimeProvider = new TickTimeProvider(tickTimeRepositoryMock, timeWatchMock);
    }

    private TestObserver<Long> subscribe() {
        return tickTimeProvider
            .get()
            .test();
    }

    private OngoingStubbing<Single<Long>> stubGetLatestFromRepository() {
        return when(tickTimeRepositoryMock.get());
    }

    @Test
    public void getCallIsDeferred() {
        tickTimeProvider.get();

        verifyZeroInteractions(tickTimeRepositoryMock);
        verifyZeroInteractions(timeWatchMock);
    }

    @Test
    public void whenRepositoryFailsErrorIsPropagated() {
        stubGetLatestFromRepository().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    @Test
    public void whenRepositorySucceedsValueFromTimeWatchIsReturned() {
        stubGetLatestFromRepository().thenReturn(Single.just(tickTime));
        when(timeWatchMock.getForNewTime(tickTime)).thenReturn(watchTime);

        subscribe()
            .assertValue(watchTime)
            .assertComplete();
    }
}
