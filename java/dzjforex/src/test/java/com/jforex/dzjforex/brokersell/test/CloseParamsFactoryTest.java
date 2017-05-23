package com.jforex.dzjforex.brokersell.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokersell.CloseParamsFactory;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.CloseParams;

public class CloseParamsFactoryTest extends CommonUtilForTest {

    private CloseParamsFactory closeParamsFactory;

    private CloseParams closeParams;

    @Before
    public void setUp() {
        closeParamsFactory = new CloseParamsFactory(tradeUtilityMock);

        closeParams = (CloseParams) closeParamsFactory
            .get(orderMockA, brokerSellData)
            .test()
            .getEvents()
            .get(0)
            .get(0);
    }

    @Test
    public void assertCloseParamsValues() {
        assertThat(closeParams.order(), equalTo(orderMockA));
        assertThat(closeParams.partialCloseAmount(), equalTo(brokerSellData.amount()));
    }

    @Test
    public void assertComposeParams() throws Exception {
        assertComposeParamsForTask(closeParams);
    }
}
