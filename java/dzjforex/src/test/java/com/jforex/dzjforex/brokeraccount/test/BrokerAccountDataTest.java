package com.jforex.dzjforex.brokeraccount.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokeraccount.BrokerAccountData;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

public class BrokerAccountDataTest extends CommonUtilForTest {

    private BrokerAccountData brokerAccountData;

    @Mock
    private AccountInfo accountInfoMock;
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

    @Test
    public void baseEquityIsCorrectFilled() {
        assertThat(accountInfoParams[0], equalTo(baseEquity));
    }

    @Test
    public void tradeValueIsCorrectFilled() {
        assertThat(accountInfoParams[1], equalTo(tradeValue));
    }

    @Test
    public void usedMarginIsCorrectFilled() {
        assertThat(accountInfoParams[2], equalTo(usedMargin));
    }
}
