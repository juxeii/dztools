package com.jforex.dzjforex.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Lists;
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
    @Mock
    private IOrder noZorroOrderMock;
    private final int orderID = 1;
    private final List<IOrder> openOrders = Lists.newArrayList();
    private final List<IOrder> historyOrders = Lists.newArrayList();

    @Before
    public void setUp() {
        orderLookup = new OrderLookup(orderRepositoryMock,
                                      openOrdersMock,
                                      historyOrdersMock);

        doAnswer(invocation -> {
            makeRepositoryPass();
            return null;
        }).when(orderRepositoryMock).importZorroOrders(openOrders);
    }

    private void makeRepositoryPass() {
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Single.just(orderMock));
    }

    private void makeRepositoryFail() {
        when(orderRepositoryMock.getByID(orderID)).thenReturn(Single.error(jfException));
    }

    private void makeOpenOrdersPass() {
        openOrders.add(orderMock);
        doAnswer(invocation -> {
            makeRepositoryPass();
            return null;
        })
            .when(orderRepositoryMock).importZorroOrders(openOrders);
        when(openOrdersMock.get()).thenReturn(Single.just(openOrders));
    }

    private void makeOpenOrdersFail() {
        when(openOrdersMock.get()).thenReturn(Single.error(jfException));
    }

    private void makeHistoryOrdersPass() {
        historyOrders.add(orderMock);
        doAnswer(invocation -> {
            makeRepositoryPass();
            return null;
        })
            .when(orderRepositoryMock).importZorroOrders(historyOrders);
        when(historyOrdersMock.get()).thenReturn(Single.just(historyOrders));
    }

    private void makeHistoryOrdersFail() {
        when(historyOrdersMock.get()).thenReturn(Single.error(jfException));
    }

    private TestObserver<IOrder> testObserver() {
        return orderLookup
            .getByID(orderID)
            .test();
    }

    @Test
    public void orderFromRepositoryIsReturned() {
        makeRepositoryPass();

        testObserver().assertValue(orderMock);
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
            public void onHistoryOrdersFailErrorIsPropagated() {
                makeHistoryOrdersFail();

                testObserver().assertError(JFException.class);
            }

            public class OnHistoryOrdersOK {

                @Before
                public void setUp() {
                    makeHistoryOrdersPass();
                }

                @Test
                public void historyOrdersAreReturned() {
                    testObserver().assertValue(orderMock);
                }

                @Test
                public void importToRepositoryCalled() {
                    testObserver();

                    verify(orderRepositoryMock).importZorroOrders(historyOrders);
                }

                @Test
                public void onSecondLookUpOrderIsFromCache() {
                    testObserver();
                    testObserver().assertValue(orderMock);

                    verify(openOrdersMock).get();
                    verify(historyOrdersMock).get();
                }
            }
        }

        public class OnOpenOrdersOK {

            @Before
            public void setUp() {
                makeOpenOrdersPass();
            }

            @Test
            public void openOrdersAreReturned() {
                testObserver().assertValue(orderMock);
            }

            @Test
            public void importToRepositoryCalled() {
                testObserver();

                verify(orderRepositoryMock).importZorroOrders(openOrders);
            }

            @Test
            public void historyOrdersNotCalled() {
                testObserver();

                verifyZeroInteractions(historyOrderMock);
            }

            @Test
            public void onSecondLookUpOrderIsFromCache() {
                testObserver();
                testObserver().assertValue(orderMock);

                verify(openOrdersMock).get();
            }
        }
    }
}
