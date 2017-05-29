package com.jforex.dzjforex.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.order.OpenOrdersProvider;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OpenOrdersProviderTest extends CommonUtilForTest {

    private OpenOrdersProvider openOrdersProvider;

    private final List<IOrder> ordersFromEngine = Lists.newArrayList();

    @Before
    public void setUp() {
        openOrdersProvider = new OpenOrdersProvider(engineMock, pluginConfigMock);
    }

    private OngoingStubbing<List<IOrder>> stubGetOrdersFromEngine() throws JFException {
        return when(engineMock.getOrders());
    }

    private TestObserver<List<IOrder>> subscribe() {
        return openOrdersProvider
            .get()
            .test();
    }

    @Test
    public void getCallIsDeferred() {
        openOrdersProvider.get();

        verifyZeroInteractions(engineMock);
    }

    @Test
    public void whenEngineFetchFailsRetriesAreDone() throws JFException {
        makeStubFailRetriesThenSuccess(stubGetOrdersFromEngine(), ordersFromEngine);

        subscribe();

        advanceRetryTimes();
        verify(engineMock, times(historyAccessRetries + 1)).getOrders();
    }

    @Test
    public void whenEngineSucceedsOrdersAreReturned() throws JFException {
        stubGetOrdersFromEngine().thenReturn(ordersFromEngine);

        subscribe()
            .assertValue(ordersFromEngine)
            .assertComplete();
    }
}
