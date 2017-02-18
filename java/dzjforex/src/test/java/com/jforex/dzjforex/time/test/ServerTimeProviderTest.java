package com.jforex.dzjforex.time.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.time.NTPProvider;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.dzjforex.time.TickTimeProvider;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ServerTimeProviderTest extends CommonUtilForTest {

    private ServerTimeProvider serverTimeProvider;

    @Mock
    private NTPProvider ntpProviderMock;
    @Mock
    private TickTimeProvider tickTimeProviderMock;
    @Mock
    private Clock clockMock;
    private final long latestNTP = 42L;
    private long latestTickTime = 12L;
    private final long currentTime = 5L;

    @Before
    public void setUp() {
        when(tickTimeProviderMock.get()).thenReturn(latestTickTime);

        when(clockMock.millis()).thenReturn(currentTime);

        serverTimeProvider = new ServerTimeProvider(ntpProviderMock,
                                                    tickTimeProviderMock,
                                                    clockMock);
    }

    public class InvalidNTPTime {

        @Before
        public void setUp() {
            when(ntpProviderMock.get()).thenReturn(Constant.INVALID_SERVER_TIME);
        }

        @Test
        public void latestTickTimeIsReturned() {
            assertThat(serverTimeProvider.get(), equalTo(latestTickTime));
        }

        public class InvalidNTPTimeForSecondCall {

            @Before
            public void setUp() {
                latestTickTime = 24L;

                when(tickTimeProviderMock.get()).thenReturn(latestTickTime);
            }

            @Test
            public void latestTickTimeIsReturned() {
                assertThat(serverTimeProvider.get(), equalTo(latestTickTime));
            }
        }

        public class ValidNTPTimeForSecondCall {

            @Before
            public void setUp() {
                when(ntpProviderMock.get()).thenReturn(latestNTP);
            }

            @Test
            public void latestNTPIsReturned() {
                assertThat(serverTimeProvider.get(), equalTo(latestNTP));
            }
        }
    }

    public class ValidNTPTime {

        @Before
        public void setUp() {
            when(ntpProviderMock.get()).thenReturn(latestNTP);
        }

        @Test
        public void latestNTPIsReturned() {
            assertThat(serverTimeProvider.get(), equalTo(latestNTP));
        }

        public class WhenGetCalled {

            @Before
            public void setUp() {
                serverTimeProvider.get();
            }

            @Test
            public void nextGetReturnsLatestNTPPlusElapsedClockTime() {
                final long nextCurrentTime = 90L;
                when(clockMock.millis()).thenReturn(nextCurrentTime);
                final long passedTime = nextCurrentTime - currentTime;

                assertThat(serverTimeProvider.get(), equalTo(latestNTP + passedTime));
            }
        }
    }
}
