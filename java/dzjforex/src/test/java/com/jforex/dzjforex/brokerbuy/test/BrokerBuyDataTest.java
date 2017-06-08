package com.jforex.dzjforex.brokerbuy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import io.reactivex.Maybe;

public class BrokerBuyDataTest extends CommonUtilForTest {

    private BrokerBuyData brokerBuyData;

    private final double tradeParams[] = new double[1];
    private final int contracts = 12500;
    private final double amount = 0.0125;
    private final double dStopDist = 0.0034;
    private final double openPrice = 1.12345;
    private final OrderCommand orderCommand = OrderCommand.BUY;

    @Before
    public void setUp() {
        setUpMocks();

        brokerBuyData = new BrokerBuyData(instrumentNameForTest,
                                          contracts,
                                          dStopDist,
                                          tradeParams,
                                          tradeUtilityMock);

        brokerBuyData.fillOpenPrice(orderMockA);
    }

    private void setUpMocks() {
        when(orderLabelUtilMock.create()).thenReturn(orderLabel);
        when(orderLabelUtilMock.idFromLabel(orderLabel)).thenReturn(Maybe.just(orderID));

        when(orderMockA.getOpenPrice()).thenReturn(openPrice);

        when(tradeUtilityMock.contractsToAmount(contracts)).thenReturn(amount);
        when(tradeUtilityMock.orderCommandForContracts(contracts)).thenReturn(orderCommand);
    }

    @Test
    public void openPriceIsCorrectFilled() {
        assertThat(tradeParams[0], equalTo(openPrice));
    }

    @Test
    public void assertInstrumentName() {
        assertThat(brokerBuyData.assetName(), equalTo(instrumentNameForTest));
    }

    @Test
    public void assertStopDistance() {
        assertThat(brokerBuyData.slDistance(), equalTo(dStopDist));
    }

    @Test
    public void assertAmount() {
        assertThat(brokerBuyData.amount(), equalTo(amount));
    }

    @Test
    public void assertOrderLabel() {
        assertThat(brokerBuyData.orderLabel(), equalTo(orderLabel));
    }

    @Test
    public void assertOrderID() {
        assertThat(brokerBuyData.orderID(), equalTo(orderID));
    }

    @Test
    public void assertOrderCommand() {
        assertThat(brokerBuyData.orderCommand(), equalTo(orderCommand));
    }
}
