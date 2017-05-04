package com.jforex.dzjforex.time.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.test.util.RxTestUtil;
import com.jforex.dzjforex.time.NTPFetch;
import com.jforex.dzjforex.time.NTPProvider;

import io.reactivex.Single;

public class NTPProviderTest extends CommonUtilForTest {

    private NTPProvider ntpProvider;

    @Mock
    private NTPFetch ntpFetchMock;

    private static final long ntpSynchInterval = 5000L;
    private static final long firstNTP = 12;
    private static final long secondNTP = 24;
    private static final long thirdNTP = 42;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(pluginConfigMock.ntpSynchInterval()).thenReturn(ntpSynchInterval);

        when(ntpFetchMock.observable()).thenReturn(Single.just(firstNTP),
                                                   Single.just(secondNTP),
                                                   Single.just(thirdNTP),
                                                   Single.error(new Exception()));

        ntpProvider = new NTPProvider(ntpFetchMock, pluginConfigMock);
    }

    private void synchAndAssertTime(final long expectedNTP) {
        RxTestUtil.advanceTimeInMillisBy(ntpSynchInterval);
        assertThat(ntpProvider.get(), equalTo(expectedNTP));
    }

    @Test
    public void synchTaskIsStartedAfterCreation() {
        RxTestUtil.advanceTimeInMillisBy(1L);

        assertThat(ntpProvider.get(), equalTo(firstNTP));
    }

    @Test
    public void ntpIsCorrectForIntervals() {
        synchAndAssertTime(firstNTP);
        synchAndAssertTime(firstNTP);
        synchAndAssertTime(firstNTP);

        verify(ntpFetchMock).observable();
    }
}
