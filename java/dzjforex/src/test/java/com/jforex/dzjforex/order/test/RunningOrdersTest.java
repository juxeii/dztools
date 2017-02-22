package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.order.RunningOrders;
import com.jforex.dzjforex.test.util.CommonOrderForTest;

public class RunningOrdersTest extends CommonOrderForTest {

    private RunningOrders runningOrders;

    @Before
    public void setUp() {
        runningOrders = new RunningOrders(engineMock);
    }

    @Test
    public void onEngineErrorEmptyListIsReturned() throws JFException {
        when(engineMock.getOrders()).thenThrow(jfException);

        assertThat(runningOrders.get().size(), equalTo(0));

        verify(engineMock).getOrders();
    }

    @Test
    public void getReturnsOrdersFromyList() throws JFException {
        final List<IOrder> orderList = new ArrayList<>();
        when(engineMock.getOrders()).thenReturn(orderList);

        assertThat(runningOrders.get(), equalTo(orderList));

        verify(engineMock).getOrders();
    }
}
