package com.jforex.dzjforex.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderRepositoryTest extends CommonUtilForTest {

    private OrderRepository orderRepository;

    private final int orderID = 42;
    private final List<IOrder> orders = Lists.newArrayList();

    @Before
    public void setUp() {
        orders.add(orderMockA);
        orders.add(orderMockB);

        orderRepository = new OrderRepository(orderLabelUtilMock);
    }

    private TestObserver<IOrder> subscribeGetByID() {
        return orderRepository
            .getByID(orderID)
            .test();
    }

    private TestObserver<Void> subscribeOrderStore() {
        return orderRepository
            .store(orderMockA)
            .test();
    }

    private TestObserver<Void> subscribeOrdersStore() {
        return orderRepository
            .store(orders)
            .test();
    }

    private OngoingStubbing<Maybe<Integer>> stubIDFromOrder(final IOrder orderMock) {
        return when(orderLabelUtilMock.idFromOrder(orderMock));
    }

    @Test
    public void orderIDNotFoundWhenNotStoredBefore() {
        subscribeGetByID().assertNoValues();
    }

    @Test
    public void whenOrderIsStoredBeforeTheIDIsFound() {
        stubIDFromOrder(orderMockA).thenReturn(Maybe.just(orderID));

        subscribeOrderStore();

        subscribeGetByID().assertValue(orderMockA);
    }

    @Test
    public void storeOrdersFilterCorrect() {
        stubIDFromOrder(orderMockA).thenReturn(Maybe.empty());
        stubIDFromOrder(orderMockB).thenReturn(Maybe.just(orderID));

        subscribeOrdersStore().assertComplete();

        subscribeGetByID().assertValue(orderMockB);
    }

    @Test
    public void storeOrdersFilterCorrectWhenAllOrdersAreNotZorroOrders() {
        stubIDFromOrder(orderMockA).thenReturn(Maybe.empty());
        stubIDFromOrder(orderMockB).thenReturn(Maybe.empty());

        subscribeOrdersStore().assertComplete();

        subscribeGetByID().assertNoValues();
    }

    public class WhenNoZorroOrder {

        @Before
        public void setUp() {
            stubIDFromOrder(orderMockA).thenReturn(Maybe.empty());
        }

        @Test
        public void storeCompletesWithoutError() {
            subscribeOrderStore().assertComplete();
        }

        @Test
        public void orderIsNotStored() {
            subscribeOrderStore();

            subscribeGetByID().assertNoValues();
        }
    }
}
