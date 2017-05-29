package com.jforex.dzjforex.order.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.order.OrderIDLookUp;
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
    private OrderIDLookUp openOrdersMock;
    @Mock
    private OrderIDLookUp historyOrdersMock;
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

    private OngoingStubbing<Maybe<IOrder>> stubRepoGetByID() {
        return when(orderRepositoryMock.getByID(orderID));
    }

    private TestObserver<IOrder> subscribe() {
        return orderLookup
            .getByID(orderID)
            .test();
    }

    @Test
    public void orderFromRepositoryIsReturned() {
        stubRepoGetByID().thenReturn(Maybe.just(orderMockA));

        subscribe().assertValue(orderMockA);
    }

    public class OnRepositoryFail {

        @Before
        public void setUp() {
            stubRepoGetByID().thenReturn(Maybe.empty());
        }

        private OngoingStubbing<Maybe<IOrder>> stubOpenOrdersGetByID() {
            return when(openOrdersMock.getByID(orderID));
        }

        public class OnOpenOrdersFail {

            @Before
            public void setUp() {
                stubOpenOrdersGetByID().thenReturn(Maybe.empty());
            }

            private OngoingStubbing<Maybe<IOrder>> stubHistoryOrdersGetByID() {
                return when(historyOrdersMock.getByID(orderID));
            }

            @Test
            public void whenHistoryOrdersReturnNoOrderAnEmptyMaybeIsReturned() {
                stubHistoryOrdersGetByID().thenReturn(Maybe.empty());

                subscribe().assertNoValues();
            }

            @Test
            public void onHistoryLookupOKOrderIsReturned() {
                stubHistoryOrdersGetByID().thenReturn(Maybe.just(orderMockA));

                subscribe().assertValue(orderMockA);
            }
        }

        public class OnOpenOrdersOK {

            @Before
            public void setUp() {
                stubOpenOrdersGetByID().thenReturn(Maybe.just(orderMockA));
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
