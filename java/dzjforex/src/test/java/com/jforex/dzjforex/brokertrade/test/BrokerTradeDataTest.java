package com.jforex.dzjforex.brokertrade.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokertrade.BrokerTradeData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerTradeDataTest extends CommonUtilForTest {

    private BrokerTradeData brokerTradeData;

    private final double tradeParams[] = new double[4];
    private final int nTradeID = 42;
    private final double pOpen = 1.1203;
    private final double pClose = 1.1245;
    private final double pRoll = 3.456;
    private final double pProfit = 23.45;

    @Before
    public void setUp() {
        brokerTradeData = new BrokerTradeData(nTradeID, tradeParams);

        brokerTradeData.fill(pOpen,
                             pClose,
                             pRoll,
                             pProfit);
    }

    private void assertFillValueAtIndex(final double value,
                                        final int index) {
        assertThat(tradeParams[index], equalTo(value));
    }

    @Test
    public void assertTradeID() {
        assertThat(brokerTradeData.nTradeID(), equalTo(nTradeID));
    }

    @Test
    public void openIsCorrectFilled() {
        assertFillValueAtIndex(pOpen, 0);
    }

    @Test
    public void closeIsCorrectFilled() {
        assertFillValueAtIndex(pClose, 1);
    }

    @Test
    public void rollIsCorrectFilled() {
        assertFillValueAtIndex(pRoll, 2);
    }

    @Test
    public void profitIsCorrectFilled() {
        assertFillValueAtIndex(pProfit, 3);
    }
}