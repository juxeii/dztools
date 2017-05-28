package com.jforex.dzjforex.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.history.HistoryOrders;
import com.jforex.dzjforex.order.OpenOrders;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OpenOrdersTest extends CommonUtilForTest {

    private OpenOrders openOrders;

    @Mock
    private OrderRepository orderRepositoryMock;
    @Mock
    private OpenOrders openOrdersMock;
    @Mock
    private HistoryOrders historyOrdersMock;
    @Mock
    private IOrder storedOrderMock;
    @Mock
    private IOrder runningOrderMock;
    @Mock
    private IOrder historyOrderMock;
    private final List<IOrder> ordersFromEngine = Lists.newArrayList();
    private final int orderID = 1;

    @Before
    public void setUp() {
        openOrders = new OpenOrders(engineMock,
                                    orderRepositoryMock,
                                    pluginConfigMock);
    }

    private TestObserver<IOrder> subscribe() {
        return openOrders
            .getByID(orderID)
            .test();
    }

    private OngoingStubbing<List<IOrder>> stubGetOrdersFromEngine() throws JFException {
        return when(engineMock.getOrders());
    }

    @Test
    public void getByIDCallIsDeferred() {
        openOrders.getByID(orderID);

        verifyZeroInteractions(engineMock);
        verifyZeroInteractions(orderRepositoryMock);
        verifyZeroInteractions(pluginConfigMock);
    }

    @Test
    public void whenEngineFetchFailsRetriesAreDone() throws JFException {
        makeStubFailRetriesThenSuccess(stubGetOrdersFromEngine(), ordersFromEngine);

        subscribe();

        advanceRetryTimes();
        verify(engineMock, times(historyAccessRetries + 1)).getOrders();
    }

    @Test
    public void whenEngineFetchFailsNoErrorIsPropagated() throws JFException {
        setHistoryRetries(0);
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Maybe.empty());
        stubGetOrdersFromEngine().thenThrow(jfException);

        subscribe()
            .assertNoErrors()
            .assertNoValues();
    }

    public class WhenEngineSucceeds {

        @Before
        public void setUp() throws JFException {
            stubGetOrdersFromEngine().thenReturn(ordersFromEngine);
        }

        private OngoingStubbing<Maybe<IOrder>> stubGetOrderByID() {
            return when(orderRepositoryMock.getByID(orderID));
        }

        @Test
        public void ordersAreStoredAtRepository() {
            subscribe();

            verify(orderRepositoryMock).store(ordersFromEngine);
        }

        @Test
        public void whenOrderIDWasFoundTheOrderIsReturned() {
            stubGetOrderByID().thenReturn(Maybe.just(orderMockA));

            subscribe().assertValue(orderMockA);
        }

        @Test
        public void whenOrderIDWasNotFoundNoOrderIsReturned() {
            stubGetOrderByID().thenReturn(Maybe.empty());

            subscribe().assertNoValues();
        }
    }
}
