package com.jforex.dzjforex.brokersell.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokersell.CloseParamsFactory;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class CloseParamsFactoryTest extends CommonUtilForTest {

    private CloseParamsFactory closeParamsFactory;

    private CloseParams closeParams;
    private final double closeAmount = 0.045;

    @Before
    public void setUp() {
        when(brokerSellDataMock.amount()).thenReturn(closeAmount);

        closeParamsFactory = new CloseParamsFactory(orderLabelUtilMock, retryParamsMock);

        closeParams = (CloseParams) closeParamsFactory
            .get(orderMockA, brokerSellDataMock)
            .test()
            .getEvents()
            .get(0)
            .get(0);
    }

    @Test
    public void assertCloseParamsValues() {
        assertThat(closeParams.order(), equalTo(orderMockA));
        assertThat(closeParams.partialCloseAmount(), equalTo(closeAmount));
    }

    @Test
    public void assertComposeParams() throws Exception {
        assertComposeParamsForTask(closeParams);
    }
}
