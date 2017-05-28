package com.jforex.dzjforex.brokeraccount.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokeraccount.BrokerAccountData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerAccountDataTest extends CommonUtilForTest {

    private BrokerAccountData brokerAccountData;

    private final double accountInfoParams[] = new double[3];
    private final double baseEquity = 123.45;
    private final double tradeValue = 321.98;
    private final double usedMargin = 42.42;

    @Before
    public void setUp() {
        setUpAccountInfo();

        brokerAccountData = new BrokerAccountData(accountInfoParams);

        brokerAccountData.fill(accountInfoMock);
    }

    private void setUpAccountInfo() {
        when(accountInfoMock.baseEquity()).thenReturn(baseEquity);
        when(accountInfoMock.tradeValue()).thenReturn(tradeValue);
        when(accountInfoMock.usedMargin()).thenReturn(usedMargin);
    }

    private void assertFillValueAtIndex(final double value,
                                        final int index) {
        assertThat(accountInfoParams[index], equalTo(value));
    }

    @Test
    public void baseEquityIsCorrectFilled() {
        assertFillValueAtIndex(baseEquity, 0);
    }

    @Test
    public void tradeValueIsCorrectFilled() {
        assertFillValueAtIndex(tradeValue, 1);
    }

    @Test
    public void usedMarginIsCorrectFilled() {
        assertFillValueAtIndex(usedMargin, 2);
    }
}
