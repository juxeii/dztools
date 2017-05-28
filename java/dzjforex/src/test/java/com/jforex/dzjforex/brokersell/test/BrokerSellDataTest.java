package com.jforex.dzjforex.brokersell.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokersell.BrokerSellData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerSellDataTest extends CommonUtilForTest {

    private BrokerSellData brokerSellData;

    private final int orderID = 42;
    private final double amount = 0.0125;

    @Before
    public void setUp() {
        brokerSellData = new BrokerSellData(orderID, amount);
    }

    @Test
    public void assertOrderID() {
        assertThat(brokerSellData.orderID(), equalTo(orderID));
    }

    @Test
    public void assertAmount() {
        assertThat(brokerSellData.amount(), equalTo(amount));
    }
}
