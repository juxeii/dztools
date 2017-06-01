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
    private final LocalDateTime firstSubmitTime = LocalDateTime.of(2017, Month.APRIL, 8, 12, 22);
    private final LocalDateTime secondSubmitTime = LocalDateTime.of(2017, Month.APRIL, 8, 13, 24);
    private long firstsubmitMillis;
    private long secondSubmitMillis;

    @Before
    public void setUp() {
        firstsubmitMillis = DateTimeUtil.millisFromDateTime(firstSubmitTime);
        secondSubmitMillis = DateTimeUtil.millisFromDateTime(secondSubmitTime);

        when(pluginConfigMock.dummySubmitMinuteForHour()).thenReturn(2);

        dummySubmit = new DummySubmit(dummySubmitRunnerMock, pluginConfigMock);
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

    public class WhenFirstSubmitTime {

        @Before
        public void setUp() {
            dummySubmit.wasOffline(firstsubmitMillis);
        }

        @Test
        public void submitWasCalledOnRunner() {
            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void noNextSubmitWithinOneMinute() {
            dummySubmit.wasOffline(firstsubmitMillis + 59999);

            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void noNextSubmitWithinTwoMinutes() {
            dummySubmit.wasOffline(firstsubmitMillis + (2 * 60000) - 1);

            verify(dummySubmitRunnerMock).start();
        }

        @Test
        public void nextSubmitOnSecondSubmitTime() {
            dummySubmit.wasOffline(secondSubmitMillis);

            verify(dummySubmitRunnerMock, times(2)).start();
        }
    }
}
