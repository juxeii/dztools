package com.jforex.dzjforex.brokertime.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.DummySubmit;
import com.jforex.dzjforex.brokertime.DummySubmitRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.misc.DateTimeUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class DummySubmitTest extends CommonUtilForTest {

    private DummySubmit dummySubmit;

    @Mock
    private DummySubmitRunner dummySubmitRunnerMock;
    private final LocalDateTime halfHourTime = LocalDateTime.of(2017, Month.APRIL, 8, 12, 30);
    private final LocalDateTime fullHourTime = LocalDateTime.of(2017, Month.APRIL, 8, 13, 00);
    private long halfHourMillis;
    private long fullHourMillis;

    @Before
    public void setUp() {
        halfHourMillis = DateTimeUtil.millisFromDateTime(halfHourTime);
        fullHourMillis = DateTimeUtil.millisFromDateTime(fullHourTime);

        dummySubmit = new DummySubmit(dummySubmitRunnerMock);
    }

    private void assertIsMarketOffline(final boolean isOffline,
                                       final long serverTime) {
        assertThat(dummySubmit.wasOffline(serverTime), equalTo(isOffline));
    }

    private OngoingStubbing<Boolean> stubWasOffline() {
        return when(dummySubmitRunnerMock.wasOffline());
    }

    @Test
    public void whenRunnerIsOfflineMarketIsClosed() {
        stubWasOffline().thenReturn(true);

        assertIsMarketOffline(true, 42L);
    }

    @Test
    public void whenRunnerIsOnlineMarketIsNotClosed() {
        stubWasOffline().thenReturn(false);

        assertIsMarketOffline(false, 42L);
    }

    public class WhenHalfHourServerTime {

        @Before
        public void setUp() {
            dummySubmit.wasOffline(halfHourMillis);
        }

        @Test
        public void submitWasCalledOnRunner() {
            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void noNextSubmitWithinOneMinute() {
            dummySubmit.wasOffline(halfHourMillis + 59999);

            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void noNextSubmitWithin15Minutes() {
            dummySubmit.wasOffline(halfHourMillis + (15 * 60000));

            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void nextSubmitOnFullHourTime() {
            dummySubmit.wasOffline(fullHourMillis);

            verify(dummySubmitRunnerMock, times(2)).start();
        }
    }

    public class WhenFullHourServerTime {

        @Before
        public void setUp() {
            dummySubmit.wasOffline(fullHourMillis);
        }

        @Test
        public void submitWasCalledOnRunner() {
            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void noNextSubmitWithinOneMinute() {
            dummySubmit.wasOffline(halfHourMillis + 59999);

            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void noNextSubmitWithin15Minutes() {
            dummySubmit.wasOffline(halfHourMillis + (15 * 60000));

            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void nextSubmitOnHalfHourTime() {
            dummySubmit.wasOffline(fullHourMillis + (30 * 60000));

            verify(dummySubmitRunnerMock, times(2)).start();
        }
    }
}
