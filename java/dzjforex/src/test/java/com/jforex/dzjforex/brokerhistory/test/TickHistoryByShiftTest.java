package com.jforex.dzjforex.brokerhistory.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.ITick;
import com.jforex.dzjforex.brokerhistory.HistoryFetchDate;
import com.jforex.dzjforex.brokerhistory.TickHistoryByShift;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class TickHistoryByShiftTest extends BarsAndTicksForTest {

    private TickHistoryByShift tickHistoryByShift;

    @Mock
    private HistoryWrapper historyWrapperMock;
    @Mock
    private HistoryFetchDate historyFetchDateMock;
    private final long startDate = 12L;
    private final long endDate = 42L;
    private final int shift = 10;

    @Before
    public void setUp() {
        tickHistoryByShift = new TickHistoryByShift(historyWrapperMock,
                                                    historyFetchDateMock,
                                                    pluginConfigMock);
    }

    private TestObserver<List<ITick>> subscribe() {
        return tickHistoryByShift
            .get(instrumentForTest,
                 endDate,
                 shift)
            .test();
    }

    private OngoingStubbing<Observable<Long>> stubFetchDate() {
        return when(historyFetchDateMock.startDatesForTick(instrumentForTest, endDate));
    }

    private OngoingStubbing<Single<List<ITick>>> stubGetTicks() {
        return when(historyWrapperMock.getTicksReversed(instrumentForTest,
                                                        startDate,
                                                        startDate + tickFetchMillis - 1));
    }

    @Test
    public void getCallIsDeferred() {
        tickHistoryByShift.get(instrumentForTest,
                               endDate,
                               shift);

        verifyZeroInteractions(historyFetchDateMock);
        verifyZeroInteractions(historyWrapperMock);
    }

    @Test
    public void whenHistoryFetchDateFailsRetriesAreDone() {
        stubGetTicks().thenReturn(Single.just(tickMockList));

        makeObservableStubFailRetriesThenSuccess(stubFetchDate(), endDate);

        subscribe();

        advanceRetryTimes();
        verify(historyFetchDateMock, times(historyAccessRetries + 1)).startDatesForTick(instrumentForTest, endDate);
    }

    public class WhenHistoryFetchDateSucceeds {

        @Before
        public void setUp() {
            stubFetchDate().thenReturn(Observable.just(startDate));
        }

        @Test
        public void whenGetTicksFailsRetriesAreDone() {
            makeSingleStubFailRetriesThenSuccess(stubGetTicks(), tickMockList);

            subscribe();

            advanceRetryTimes();
            verify(historyWrapperMock, times(historyAccessRetries + 1))
                .getTicksReversed(instrumentForTest,
                                  startDate,
                                  startDate + tickFetchMillis - 1);
        }

        @Test
        public void getReturnsTickListWhenGetTicksSucceeds() {
            stubGetTicks().thenReturn(Single.just(tickMockList));

            subscribe().assertValue(tickMockList);
        }

        @Test
        public void getReturnsNoMoreThanNoOfShiftPlusOneTicks() {
            final List<ITick> tooManyTicks = Stream
                .generate(() -> mock(ITick.class))
                .limit(shift + 20)
                .collect(Collectors.toList());

            stubGetTicks().thenReturn(Single.just(tooManyTicks));

            final int returnedListSize = subscribe()
                .values()
                .get(0)
                .size();
            assertThat(returnedListSize, equalTo(shift + 1));
        }
    }
}
