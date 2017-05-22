package com.jforex.dzjforex.brokerbuy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

public class BrokerBuyDataTest extends CommonUtilForTest {

    private BrokerBuyData brokerBuyData;

    private final double tradeParams[] = new double[1];
    private final int contracts = 12500;
    private final double amount = 0.0125;
    private final double dStopDist = 0.0034;
    private final double openPrice = 1.12345;

    @Before
    public void setUp() {
        when(orderMockA.getOpenPrice()).thenReturn(openPrice);

        brokerBuyData = new BrokerBuyData(instrumentNameForTest,
                                          contracts,
                                          dStopDist,
                                          tradeParams,
                                          pluginConfigMock);

        brokerBuyData.fillOpenPrice(orderMockA);
    }

    @Test
    public void openPriceIsCorrectFilled() {
        assertThat(tradeParams[0], equalTo(openPrice));
    }

    @Test
    public void assertInstrumentName() {
        assertThat(brokerBuyData.instrumentName(), equalTo(instrumentNameForTest));
    }

    @Test
    public void assertStopDistance() {
        assertThat(brokerBuyData.dStopDist(), equalTo(dStopDist));
    }

    @Test
    public void assertAmount() {
        assertThat(brokerBuyData.amount(), equalTo(amount));
    }

    @Test
    public void orderCommandIsBuyForPositiveContracts() {
        assertThat(brokerBuyData.orderCommand(), equalTo(OrderCommand.BUY));
    }

    @Test
    public void orderCommandIsSellForNegativeContracts() {
        brokerBuyData = new BrokerBuyData(instrumentNameForTest,
                                          -contracts,
                                          dStopDist,
                                          tradeParams,
                                          pluginConfigMock);

        assertThat(brokerBuyData.orderCommand(), equalTo(OrderCommand.SELL));
    }
}
