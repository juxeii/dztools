package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.history.HistoryOrdersDates;
import com.jforex.dzjforex.history.HistoryOrdersProvider;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.misc.TimeSpan;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryOrdersProviderTest extends CommonUtilForTest {

    private HistoryOrdersProvider historyOrdersProvider;

    @Mock
    private HistoryWrapper historyWrapperMock;
    @Mock
    private BrokerSubscribe brokerSubscribeMock;
    @Mock
    private HistoryOrdersDates historyOrdersDatesMock;
    private final long from = 14L;
    private final long to = 18L;
    private final TimeSpan timeSpan = new TimeSpan(from, to);
    private final Set<Instrument> subscribedInstruments = Sets.newHashSet();
    private final List<IOrder> historyOrdersA = Lists.newArrayList();
    private final List<IOrder> historyOrdersB = Lists.newArrayList();

    @Before
    public void setUp() {
        setUpMocks();

        historyOrdersProvider = new HistoryOrdersProvider(historyWrapperMock,
                                                          brokerSubscribeMock,
                                                          historyOrdersDatesMock,
                                                          pluginConfigMock);
    }

    private void setUpMocks() {
        subscribedInstruments.add(instrumentForTest);
        subscribedInstruments.add(Instrument.EURAUD);

        historyOrdersA.add(orderMockA);
        historyOrdersB.add(orderMockB);

        when(brokerSubscribeMock.subscribedInstruments()).thenReturn(subscribedInstruments);
    }

    private TestObserver<List<IOrder>> subscribe() {
        return historyOrdersProvider
            .get()
            .test();
    }

    private OngoingStubbing<Single<List<IOrder>>> stubGetOrdersHistory(final Instrument instrument) {
        return when(historyWrapperMock.getOrdersHistory(eq(instrument), any()));
    }

    private OngoingStubbing<Single<TimeSpan>> stubGetTimeSpan() {
        return when(historyOrdersDatesMock.timeSpan());
    }

    @Test
    public void getCallIsDeferred() {
        historyOrdersProvider.get();

        verifyZeroInteractions(historyWrapperMock);
        verifyZeroInteractions(brokerSubscribeMock);
        verifyZeroInteractions(historyOrdersDatesMock);
    }

    @Test
    public void whenGetTimeSpanFailsRetriesAreDone() {
        stubGetTimeSpan().thenReturn(Single.error(jfException));

        subscribe();

        advanceRetryTimes();
        verify(historyOrdersDatesMock, times(historyAccessRetries + 1)).timeSpan();
    }

    @Test
    public void whenGetTimeSpanFailsErrorIsPropagated() {
        setHistoryRetries(0);
        stubGetTimeSpan().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenGetTimeSpanSucceeds {

        @Before
        public void setUp() {
            stubGetTimeSpan().thenReturn(Single.just(timeSpan));
        }

        @Test
        public void whenGetOrdersHistoryFailsRetriesAreDone() {
            stubGetOrdersHistory(instrumentForTest).thenReturn(Single.error(jfException));

            subscribe();

            advanceRetryTimes();
            verify(historyWrapperMock, times(historyAccessRetries + 1))
                .getOrdersHistory(any(), timeSpanCaptor.capture());
        }

        public class WhenGetOrdersHistorySucceeds {

            @Before
            public void setUp() {
                stubGetOrdersHistory(instrumentForTest).thenReturn(Single.just(historyOrdersA));
                stubGetOrdersHistory(Instrument.EURAUD).thenReturn(Single.just(historyOrdersB));
            }

            @Test
            public void twoOrdersAreReturned() {
                final List<IOrder> test = historyOrdersProvider
                    .get()
                    .blockingGet();

                assertThat(test.size(), equalTo(2));
                assertTrue(test.contains(orderMockA));
                assertTrue(test.contains(orderMockB));
            }
        }
    }
}
