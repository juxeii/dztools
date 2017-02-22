package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.order.HistoryOrders;
import com.jforex.dzjforex.test.util.CommonOrderForTest;
import com.jforex.programming.misc.DateTimeUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class HistoryOrdersTest extends CommonOrderForTest {

    private HistoryOrders historyOrders;

    @Mock
    private BrokerSubscribe brokerSubscribeMock;
    @Mock
    private IOrder orderEURUSDMockA;
    @Mock
    private IOrder orderEURUSDMockB;
    @Mock
    private IOrder orderGBPJPYMock;
    private final long serverTime = 123458723L;
    private final int historyOrderDays = 3;

    @Before
    public void setUp() {
        when(serverTimeProviderMock.get()).thenReturn(serverTime);

        when(pluginConfigMock.historyOrderInDays()).thenReturn(historyOrderDays);

        historyOrders = new HistoryOrders(historyProviderMock,
                                          brokerSubscribeMock,
                                          pluginConfigMock,
                                          serverTimeProviderMock);
    }

    @Test
    public void whenNoSubscribedInstrumentsGetReturnsEmptyList() {
        assertTrue(historyOrders.get().isEmpty());
    }

    public class WithSubscribedInstruments {

        private final LocalDateTime toDate = DateTimeUtil.dateTimeFromMillis(serverTime);
        private final LocalDateTime fromDate = toDate.minusDays(historyOrderDays);
        private final long to = DateTimeUtil.millisFromDateTime(toDate);
        private final long from = DateTimeUtil.millisFromDateTime(fromDate);
        private final Set<Instrument> subscribedInstruments = new HashSet<>();

        @Before
        public void setUp() {
            subscribedInstruments.add(Instrument.EURUSD);
            subscribedInstruments.add(Instrument.GBPJPY);
            when(brokerSubscribeMock.subscribedInstruments())
                .thenReturn(subscribedInstruments);
        }

        @Test
        public void fetchDatesAreCorrect() {
            historyOrders.get();

            verify(historyProviderMock).ordersByInstrument(Instrument.EURUSD,
                                                           from,
                                                           to);
            verify(historyProviderMock).ordersByInstrument(Instrument.GBPJPY,
                                                           from,
                                                           to);
        }

        public class WithOrdersPresent {

            private final List<IOrder> ordersForEURUSD = new ArrayList<>();
            private final List<IOrder> ordersForGBPJPY = new ArrayList<>();
            private List<IOrder> returnedList;

            @Before
            public void setUp() {
                ordersForEURUSD.add(orderEURUSDMockA);
                ordersForEURUSD.add(orderEURUSDMockB);
                ordersForGBPJPY.add(orderGBPJPYMock);

                when(historyProviderMock.ordersByInstrument(Instrument.EURUSD,
                                                            from,
                                                            to))
                                                                .thenReturn(ordersForEURUSD);
                when(historyProviderMock.ordersByInstrument(Instrument.GBPJPY,
                                                            from,
                                                            to))
                                                                .thenReturn(ordersForGBPJPY);

                returnedList = historyOrders.get();
            }

            @Test
            public void fetchedThreeOrders() {
                assertThat(returnedList.size(), equalTo(3));
                assertTrue(returnedList.containsAll(ordersForEURUSD));
                assertTrue(returnedList.containsAll(ordersForGBPJPY));
            }
        }
    }
}
