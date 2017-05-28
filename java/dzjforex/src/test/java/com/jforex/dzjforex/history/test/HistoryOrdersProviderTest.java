package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
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
import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.history.HistoryOrdersProvider;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.misc.DateTimeUtil;

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
    private ServerTimeProvider serverTimeProviderMock;
    private LocalDateTime toDateTime;
    private LocalDateTime fromDateTime;
    private long toDate;
    private long fromDate;
    private final int historyOrderInDays = 3;
    private final Set<Instrument> subscribedInstruments = Sets.newHashSet();
    private final List<IOrder> historyOrdersA = Lists.newArrayList();
    private final List<IOrder> historyOrdersB = Lists.newArrayList();

    @Before
    public void setUp() {
        setUpMocks();

        toDateTime = LocalDateTime.of(2017, Month.APRIL, 8, 12, 30);
        fromDateTime = toDateTime.minusDays(historyOrderInDays);
        toDate = DateTimeUtil.millisFromDateTime(toDateTime);
        fromDate = DateTimeUtil.millisFromDateTime(fromDateTime);

        historyOrdersProvider = new HistoryOrdersProvider(historyWrapperMock,
                                                          brokerSubscribeMock,
                                                          serverTimeProviderMock,
                                                          pluginConfigMock);
    }

    private void setUpMocks() {
        subscribedInstruments.add(instrumentForTest);
        subscribedInstruments.add(Instrument.EURAUD);

        historyOrdersA.add(orderMockA);
        historyOrdersB.add(orderMockB);

        when(brokerSubscribeMock.subscribedInstruments()).thenReturn(subscribedInstruments);

        when(pluginConfigMock.historyOrderInDays()).thenReturn(historyOrderInDays);
    }

    private TestObserver<List<IOrder>> subscribe() {
        return historyOrdersProvider
            .get()
            .test();
    }

    private OngoingStubbing<Single<List<IOrder>>> stubGetOrdersHistory(final Instrument instrument) {
        return when(historyWrapperMock.getOrdersHistory(instrument,
                                                        fromDate,
                                                        toDate));
    }

    private OngoingStubbing<Single<Long>> stubServerTime() {
        return when(serverTimeProviderMock.get());
    }

    @Test
    public void getCallIsDeferred() {
        historyOrdersProvider.get();

        verifyZeroInteractions(historyWrapperMock);
        verifyZeroInteractions(brokerSubscribeMock);
        verifyZeroInteractions(serverTimeProviderMock);
    }

    @Test
    public void whenServerTimeFailsRetriesAreDone() {
        stubServerTime().thenReturn(Single.error(jfException));

        subscribe();

        advanceRetryTimes();
        verify(serverTimeProviderMock, times(historyAccessRetries + 1)).get();
    }

    public class WhenServerTimeSucceeds {

        @Before
        public void setUp() {
            stubServerTime().thenReturn(Single.just(toDate));
        }

        @Test
        public void whenServerTimeFailsRetriesAreDone() {
            stubGetOrdersHistory(instrumentForTest).thenReturn(Single.error(jfException));

            subscribe();

            advanceRetryTimes();
            verify(historyWrapperMock, times(historyAccessRetries + 1)).getOrdersHistory(any(),
                                                                                         eq(fromDate),
                                                                                         eq(toDate));
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
