package com.jforex.dzjforex.history.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.history.HistoryOrders;
import com.jforex.dzjforex.history.HistoryOrdersProvider;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryOrdersTest extends CommonUtilForTest {

    private HistoryOrders historyOrders;

    @Mock
    private HistoryOrdersProvider historyOrdersProviderMock;
    @Mock
    private OrderRepository orderRepositoryMock;
    private final int orderID = 42;

    @Before
    public void setUp() {
        historyOrders = new HistoryOrders(historyOrdersProviderMock, orderRepositoryMock);
    }

    private TestObserver<IOrder> subscribe() {
        return historyOrders
            .getByID(orderID)
            .test();
    }

    private OngoingStubbing<Single<List<IOrder>>> stubGetHistoryOrders() {
        return when(historyOrdersProviderMock.get());
    }

    @Test
    public void getByIDCallIsDeferred() {
        historyOrders.getByID(orderID);

        verifyZeroInteractions(historyOrdersProviderMock);
        verifyZeroInteractions(orderRepositoryMock);
    }

    @Test
    public void whenHistoryOrdersProviderFailsErrorIsPropagated() {
        stubGetHistoryOrders().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenHistoryOrdersProviderSucceeds {

        private final List<IOrder> historyOrders = Lists.newArrayList();

        @Before
        public void setUp() {
            stubGetHistoryOrders().thenReturn(Single.just(historyOrders));
        }

        private OngoingStubbing<Completable> stubRepositoryStore() {
            return when(orderRepositoryMock.store(historyOrders));
        }

        @Test
        public void whenOrderRepositoryFailsErrorIsPropagated() {
            stubRepositoryStore().thenReturn(Completable.error(jfException));

            subscribe().assertError(jfException);
        }

        public class WhenOrderRepositorySucceeds {

            @Before
            public void setUp() {
                stubRepositoryStore().thenReturn(Completable.complete());
            }

            private OngoingStubbing<Maybe<IOrder>> stubGetOrderID() {
                return when(orderRepositoryMock.getByID(orderID));
            }

            @Test
            public void whenOrderRepositoryReturnsNoOrderReturnValueIsEmpty() {
                stubGetOrderID().thenReturn(Maybe.empty());

                subscribe().assertNoValues();
            }

            @Test
            public void whenOrderRepositorySucceedsTheOrderIsReturned() {
                stubGetOrderID().thenReturn(Maybe.just(orderMockA));

                subscribe().assertValue(orderMockA);
            }
        }
    }
}
