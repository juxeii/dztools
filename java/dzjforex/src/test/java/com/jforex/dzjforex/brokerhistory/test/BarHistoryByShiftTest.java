package com.jforex.dzjforex.brokerhistory.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IBar;
import com.jforex.dzjforex.brokerhistory.BarHistoryByShift;
import com.jforex.dzjforex.brokerhistory.HistoryFetchDate;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BarHistoryByShiftTest extends BarsAndTicksForTest {

    private BarHistoryByShift barHistoryByShift;

    @Mock
    private HistoryWrapper historyWrapperMock;
    @Mock
    private HistoryFetchDate historyFetchDateMock;
    private final long endDate = 42L;
    private final int shift = 299;

    @Before
    public void setUp() {
        barHistoryByShift = new BarHistoryByShift(historyWrapperMock,
                                                  historyFetchDateMock,
                                                  pluginConfigMock);
    }

    private TestObserver<List<IBar>> subscribe() {
        return barHistoryByShift
            .get(barParams,
                 endDate,
                 shift)
            .test();
    }

    private OngoingStubbing<Single<Long>> stubFetchDate() {
        return when(historyFetchDateMock.endDateForBar(barParams, endDate));
    }

    private OngoingStubbing<Single<List<IBar>>> stubGetBars() {
        return when(historyWrapperMock.getBarsReversed(barParams,
                                                       endDate - shift * periodInterval,
                                                       endDate));
    }

    @Test
    public void getCallIsDeferred() {
        barHistoryByShift.get(barParams,
                              endDate,
                              shift);

        verifyZeroInteractions(historyFetchDateMock);
        verifyZeroInteractions(historyWrapperMock);
    }

    @Test
    public void whenHistoryFetchDateFailsRetriesAreDone() {
        stubGetBars().thenReturn(Single.just(barMockList));

        makeSingleStubFailRetriesThenSuccess(stubFetchDate(), endDate);

        subscribe();

        advanceRetryTimes();
        verify(historyFetchDateMock, times(historyAccessRetries + 1)).endDateForBar(barParams, endDate);
    }

    public class WhenHistoryFetchDateSucceeds {

        @Before
        public void setUp() {
            stubFetchDate().thenReturn(Single.just(endDate));
        }

        @Test
        public void whenGetBarsFailsRetriesAreDone() {
            makeSingleStubFailRetriesThenSuccess(stubGetBars(), barMockList);

            subscribe();

            advanceRetryTimes();
            verify(historyWrapperMock, times(historyAccessRetries + 1))
                .getBarsReversed(barParams,
                                 endDate - shift * periodInterval,
                                 endDate);
        }

        @Test
        public void getReturnsBarListWhenGetBarsSucceeds() {
            stubGetBars().thenReturn(Single.just(barMockList));

            subscribe().assertValue(barMockList);
        }
    }
}
