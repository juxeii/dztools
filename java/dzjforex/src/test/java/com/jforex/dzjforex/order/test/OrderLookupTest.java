package com.jforex.dzjforex.order.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.history.HistoryOrders;
import com.jforex.dzjforex.order.OpenOrders;
import com.jforex.dzjforex.order.OrderLookup;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
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
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Single.just(orderMockA));
    }

    private void makeRepositoryFail() {
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Single.error(jfException));
    }

    private void makeOpenOrdersPass() {
        when(openOrdersMock.getByID(orderID)).thenReturn(Single.just(orderMockA));
    }

    private void makeOpenOrdersFail() {
        when(openOrdersMock.getByID(orderID)).thenReturn(Single.error(jfException));
    }

    private void makeHistoryOrdersPass() {
        when(historyOrdersMock.getByID(orderID)).thenReturn(Single.just(orderMockA));
    }

    private void makeHistoryOrdersFail() {
        when(historyOrdersMock.getByID(orderID)).thenReturn(Single.error(jfException));
    }

    private TestObserver<IOrder> testObserver() {
        return orderLookup
            .getByID(orderID)
            .test();
    }

    @Test
    public void orderFromRepositoryIsReturned() {
        makeRepositoryPass();

        testObserver().assertValue(orderMockA);
    }

    public class OnRepositoryFail {

        @Before
        public void setUp() {
            makeRepositoryFail();
        }

        public class OnOpenOrdersFail {

            @Before
            public void setUp() {
                makeOpenOrdersFail();
            }

            @Test
            public void onHistoryLookupFailErrorIsPropagated() {
                makeHistoryOrdersFail();

                testObserver().assertError(JFException.class);
            }

            @Test
            public void onHistoryLookupOKOrderIsReturned() {
                makeHistoryOrdersPass();

                testObserver().assertValue(orderMockA);
            }
        }

        public class OnOpenOrdersOK {

            @Before
            public void setUp() {
                makeOpenOrdersPass();
            }

            @Test
            public void orderFromOpenOrdersIsReturned() {
                testObserver().assertValue(orderMockA);
            }

            @Test
            public void historyOrdersNotCalled() {
                testObserver();

                verifyZeroInteractions(historyOrderMock);
            }
        }
    }
}
