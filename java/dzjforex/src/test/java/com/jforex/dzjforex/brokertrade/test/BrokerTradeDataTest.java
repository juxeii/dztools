package com.jforex.dzjforex.brokertrade.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokertrade.BrokerTradeData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerTradeDataTest extends CommonUtilForTest {

    private BrokerTradeData brokerTradeData;

    private final double tradeParams[] = new double[4];
    private final double pOpen = 1.1203;
    private final double pRoll = 0.0;
    private final double pProfit = 23.45;
    private final double price = 1.1203;
    private final double orderAmount = 0.12;

    @Before
    public void setUp() {
        setUpMocks();

        brokerTradeData = new BrokerTradeData(orderID,
                                              tradeParams,
                                              calculationUtilMock);

        brokerTradeData.fill(orderMockA);
    }

    private void setUpMocks() {
        when(calculationUtilMock.currentQuoteForOrderCommand(instrumentForTest, OrderCommand.BUY)).thenReturn(price);

        when(orderMockA.getInstrument()).thenReturn(instrumentForTest);
        when(orderMockA.getOrderCommand()).thenReturn(OrderCommand.BUY);
        when(orderMockA.getOpenPrice()).thenReturn(pOpen);
        when(orderMockA.getAmount()).thenReturn(orderAmount);
        when(orderMockA.getProfitLossInAccountCurrency()).thenReturn(pProfit);
    }

    private void assertFillValueAtIndex(final double value,
                                        final int index) {
        assertThat(tradeParams[index], equalTo(value));
    }

    @Test
    public void assertOrderID() {
        assertThat(brokerTradeData.orderID(), equalTo(orderID));
    }

    @Test
    public void openIsCorrectFilled() {
        assertFillValueAtIndex(pOpen, 0);
    }

    @Test
    public void closeIsCorrectFilled() {
        assertFillValueAtIndex(price, 1);
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