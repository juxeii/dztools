package com.jforex.dzjforex.brokertime.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.TimeWatch;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class TimeWatchTest extends CommonUtilForTest {

    private TimeWatch timeWatch;

    private final long newTime = 66L;

    @Before
    public void setUp() {
        timeWatch = new TimeWatch(clockMock);
    }

    private OngoingStubbing<Long> stubClock() {
        return when(clockMock.millis());
    }

    @Test
    public void whenNotSynchedGetReturnsGivenTime() {
        assertThat(timeWatch.getForNewTime(newTime), equalTo(newTime));
    }

    public class WhenSynched {

        private final long firstClockTime = 45L;
        private final long secondClockTime = 55L;

        @Before
        public void setUp() {
            stubClock().thenReturn(firstClockTime);

            timeWatch.getForNewTime(newTime);
        }

        @Test
        public void getReturnsSynchPlusOffsetTime() {
            stubClock().thenReturn(secondClockTime);

            final long expectedTime = newTime + (secondClockTime - firstClockTime);

            assertThat(timeWatch.getForNewTime(newTime), equalTo(expectedTime));
        }

        @Test
        public void getForBiggerNewTimeReturnsTheBiggerTime() {
            stubClock().thenReturn(secondClockTime);

            final long biggerNewTime = newTime + 200L;

            assertThat(timeWatch.getForNewTime(biggerNewTime), equalTo(biggerNewTime));
        }
    }
}
