package com.jforex.dzjforex.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.order.OpenOrders;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OpenOrdersTest extends CommonUtilForTest {

    private OpenOrders openOrders;

    private final List<IOrder> orders = Lists.newArrayList();

    @Before
    public void setUp() {
        openOrders = new OpenOrders(engineMock);
    }

    public class Get {

        private OngoingStubbing<List<IOrder>> getEngineStub() throws JFException {
            return when(engineMock.getOrders());
        }

        private TestObserver<List<IOrder>> test() {
            return openOrders
                .get()
                .test();
        }

        @Test
        public void WhenEngineIsOKOrderListIsReturned() throws JFException {
            getEngineStub().thenReturn(orders);

            test()
                .assertComplete()
                .assertValue(orders);
        }

        @Test
        public void WhenEngineFailsErrorIsPropagated() throws JFException {
            getEngineStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }
}
