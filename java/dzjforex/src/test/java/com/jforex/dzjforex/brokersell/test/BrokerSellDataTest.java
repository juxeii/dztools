package com.jforex.dzjforex.brokersell.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokersell.BrokerSellData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerSellDataTest extends CommonUtilForTest {

    private BrokerSellData brokerSellData;

    private final int contracts = 12500;
    private final double amount = 0.0125;

    @Before
    public void setUp() {
        when(tradeUtilityMock.contractsToAmount(contracts)).thenReturn(amount);

        brokerSellData = new BrokerSellData(orderID,
                                            contracts,
                                            tradeUtilityMock);
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
