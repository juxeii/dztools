package com.jforex.dzjforex.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.order.OrderIDLookUp;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderIDLookUpTest extends CommonUtilForTest {

    private OrderIDLookUp orderIDLookUp;

    @Mock
    private OrderRepository orderRepositoryMock;
    private final List<IOrder> orders = Lists.newArrayList();

    private void createSUTWithOrdersProvider(final Single<List<IOrder>> ordersProvider) {
        orderIDLookUp = new OrderIDLookUp(ordersProvider, orderRepositoryMock);
    }

    private TestObserver<IOrder> subscribe() {
        return orderIDLookUp
            .getByID(nTradeID)
            .test();
    }

    @Test
    public void getByIDCallIsDeferred() {
        createSUTWithOrdersProvider(Single.just(orders));

        orderIDLookUp.getByID(nTradeID);

        verifyZeroInteractions(orderRepositoryMock);
    }

    @Test
    public void whenHistoryOrdersProviderFailsErrorIsPropagated() {
        createSUTWithOrdersProvider(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenOrdersProviderSucceeds {

        @Before
        public void setUp() {
            createSUTWithOrdersProvider(Single.just(orders));
        }

        private OngoingStubbing<Completable> stubRepositoryStore() {
            return when(orderRepositoryMock.store(orders));
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
                return when(orderRepositoryMock.getByID(nTradeID));
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
