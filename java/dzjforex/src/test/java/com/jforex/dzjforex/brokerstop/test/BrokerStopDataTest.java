package com.jforex.dzjforex.brokerstop.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokerstop.BrokerStopData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerStopDataTest extends CommonUtilForTest {

    private BrokerStopData brokerStopData;

    private final double slPrice = 1.12345;

    @Before
    public void setUp() {
        brokerStopData = new BrokerStopData(orderID, slPrice);
    }

    @Test
    public void assertOrderID() {
        assertThat(brokerStopData.orderID(), equalTo(orderID));
    }

    @Test
    public void assertSlPrice() {
        assertThat(brokerStopData.slPrice(), equalTo(slPrice));
    }
}
