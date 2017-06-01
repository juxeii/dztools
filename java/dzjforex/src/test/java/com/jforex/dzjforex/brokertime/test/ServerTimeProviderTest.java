package com.jforex.dzjforex.brokertime.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.brokertime.ntp.NTPProvider;
import com.jforex.dzjforex.brokertime.ticktime.TickTimeProvider;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class ServerTimeProviderTest extends CommonUtilForTest {

    private ServerTimeProvider serverTimeProvider;

    @Mock
    private NTPProvider ntpProviderMock;
    @Mock
    private TickTimeProvider tickTimeProviderMock;
    private final long latestNTP = 42L;
    private final long latestTickTime = 12L;

    @Before
    public void setUp() {
        serverTimeProvider = new ServerTimeProvider(ntpProviderMock, tickTimeProviderMock);
    }

    private TestObserver<Long> subscribe() {
        return serverTimeProvider
            .get()
            .test();
    }

    private OngoingStubbing<Single<Long>> stubNTP() {
        return when(ntpProviderMock.get());
    }

    private OngoingStubbing<Single<Long>> stubTickTime() {
        return when(tickTimeProviderMock.get());
    }

    @Test
    public void getCallIsDeferred() {
        serverTimeProvider.get();

        verifyZeroInteractions(ntpProviderMock);
        verifyZeroInteractions(tickTimeProviderMock);
    }

    @Test
    public void whenNTPProviderSucceedsNTPIsReturned() {
        stubNTP().thenReturn(Single.just(latestNTP));

        subscribe().assertValue(latestNTP);
    }

    public class WhenNTPProviderFails {

        @Before
        public void setUp() {
            stubNTP().thenReturn(Single.error(jfException));
        }

        @Test
        public void whenTickTimeFailsErrorIsPropagated() {
            stubTickTime().thenReturn(Single.error(jfException));

            subscribe().assertError(jfException);
        }

        @Test
        public void whenTickTimeSucceedsTickTimeIsReturned() {
            stubTickTime().thenReturn(Single.just(latestTickTime));

            subscribe().assertValue(latestTickTime);
        }
    }
}
