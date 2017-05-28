package com.jforex.dzjforex.order.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.history.HistoryOrders;
import com.jforex.dzjforex.order.OpenOrders;
import com.jforex.dzjforex.order.OrderLookup;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderLookupTest extends CommonUtilForTest {

    private OrderLookup orderLookup;

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
    private final int orderID = 1;

    @Before
    public void setUp() {
        orderLookup = new OrderLookup(orderRepositoryMock,
                                      openOrdersMock,
                                      historyOrdersMock);
    }

    private void makeRepositoryPass() {
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Maybe.just(orderMockA));
    }

    private void makeRepositoryEmpty() {
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Maybe.empty());
    }

    private void makeOpenOrdersPass() {
        when(openOrdersMock.getByID(orderID)).thenReturn(Maybe.just(orderMockA));
    }

    private void makeOpenOrdersEmpty() {
        when(openOrdersMock.getByID(orderID)).thenReturn(Maybe.empty());
    }

    private void makeHistoryOrdersPass() {
        when(historyOrdersMock.getByID(orderID)).thenReturn(Maybe.just(orderMockA));
    }

    private void makeHistoryOrdersEmpty() {
        when(historyOrdersMock.getByID(orderID)).thenReturn(Maybe.empty());
    }

    private TestObserver<IOrder> subscribe() {
        return orderLookup
            .getByID(orderID)
            .test();
    }

    @Test
    public void orderFromRepositoryIsReturned() {
        makeRepositoryPass();

        subscribe().assertValue(orderMockA);
    }

    public class OnRepositoryFail {

        @Before
        public void setUp() {
            makeRepositoryEmpty();
        }

        public class OnOpenOrdersFail {

            @Before
            public void setUp() {
                makeOpenOrdersEmpty();
            }

            @Test
            public void whenHistoryOrdersReturnNoOrderAnEmptyMaybeIsReturned() {
                makeHistoryOrdersEmpty();

                subscribe().assertNoValues();
            }

            @Test
            public void onHistoryLookupOKOrderIsReturned() {
                makeHistoryOrdersPass();

                subscribe().assertValue(orderMockA);
            }
        }

        public class OnOpenOrdersOK {

            @Before
            public void setUp() {
                makeOpenOrdersPass();
            }

            @Test
            public void orderFromOpenOrdersIsReturned() {
                subscribe().assertValue(orderMockA);
            }

            @Test
            public void historyOrdersNotCalled() {
                subscribe();

                verifyZeroInteractions(historyOrderMock);
            }
        }
    }
}
