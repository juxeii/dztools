package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerapi.BrokerBuy;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderSubmit;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerBuyTest extends CommonUtilForTest {

    private BrokerBuy brokerBuy;

    @Mock
    private OrderSubmit orderSubmitMock;
    @Mock
    private IOrder orderMock;
    private int openTradeResult;
    private final String instrumentName = "EUR/USD";
    private final int tradeId = 1;
    private final String newLabel = "Zorro" + tradeId;
    private final Instrument tradeInstrument = Instrument.EURUSD;
    private final OrderCommand command = OrderCommand.BUY;
    private final int nAmount = 12500;
    private final double dStopDist = 0.0015;
    private final double orderAmount = 0.0125;
    private final double slPrice = 1.23457;
    private final double openPrice = 1.23466;
    private final double tradeParams[] = new double[3];

    @Before
    public void setUp() {
        tradeParams[0] = nAmount;
        tradeParams[1] = dStopDist;

        brokerBuy = new BrokerBuy(orderSubmitMock, tradeUtilMock);
    }

    private int callOpenTrade() {
        return brokerBuy.openTrade(instrumentName, tradeParams);
    }

    @Test
    public void whenTradingIsNotAllowedReturnValueIsBrokerBuyFail() {
        when(tradeUtilMock.isTradingAllowed()).thenReturn(false);

        assertThat(callOpenTrade(),
                   equalTo(ZorroReturnValues.BROKER_BUY_FAIL.getValue()));
        verifyZeroInteractions(orderSubmitMock);
    }

    public class WhenTradingIsAllowed {

        @Before
        public void setUp() {
            when(tradeUtilMock.isTradingAllowed()).thenReturn(true);
        }

        @Test
        public void whenInstrumentNameIsInvalidReturnValueIsBrokerBuyFail() {
            when(tradeUtilMock.maybeInstrumentForTrading(instrumentName))
                .thenReturn(Optional.empty());

            assertThat(callOpenTrade(),
                       equalTo(ZorroReturnValues.BROKER_BUY_FAIL.getValue()));
            verifyZeroInteractions(orderSubmitMock);
        }

        public class WhenInstrumentNameIsValid {

            private void setSubmitResult(final OrderActionResult result) {
                when(orderSubmitMock.run(tradeInstrument,
                                         command,
                                         orderAmount,
                                         newLabel,
                                         slPrice))
                                             .thenReturn(result);
            }

            private void verifySubmitCall(final double slPrice) {
                verify(orderSubmitMock).run(tradeInstrument,
                                            command,
                                            orderAmount,
                                            newLabel,
                                            slPrice);
            }

            @Before
            public void setUp() {
                when(tradeUtilMock.maybeInstrumentForTrading(instrumentName))
                    .thenReturn(Optional.of(tradeInstrument));
                when(tradeUtilMock.contractsToAmount(nAmount))
                    .thenReturn(orderAmount);
                when(tradeUtilMock.orderCommandForContracts(nAmount))
                    .thenReturn(command);
                when(tradeUtilMock.orderByID(tradeId))
                    .thenReturn(orderMock);
                when(tradeUtilMock.calculateSL(tradeInstrument,
                                               command,
                                               dStopDist))
                                                   .thenReturn(slPrice);

                when(orderLabelUtilMock.create())
                    .thenReturn(newLabel);
                when(orderLabelUtilMock.idFromLabel(newLabel))
                    .thenReturn(tradeId);

                when(orderMock.getOpenPrice())
                    .thenReturn(openPrice);
            }

            public class WhenSubmitFails {

                @Before
                public void setUp() {
                    setSubmitResult(OrderActionResult.FAIL);

                    openTradeResult = callOpenTrade();
                }

                @Test
                public void returnValueIsBrokerBuyFail() {
                    assertThat(openTradeResult,
                               equalTo(ZorroReturnValues.BROKER_BUY_FAIL.getValue()));
                }

                @Test
                public void submitIsCalled() {
                    verifySubmitCall(slPrice);
                }
            }

            public class WhenSubmitOK {

                @Before
                public void setUp() {
                    setSubmitResult(OrderActionResult.OK);
                }

                public class WhenOppositeClose {

                    @Before
                    public void setUp() {
                        tradeParams[1] = -1;

                        when(tradeUtilMock.calculateSL(tradeInstrument,
                                                       command,
                                                       dStopDist))
                                                           .thenReturn(0.0);

                        openTradeResult = callOpenTrade();
                    }

                    @Test
                    public void returnValueIsOppositeClose() {
                        assertThat(openTradeResult,
                                   equalTo(ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue()));
                    }

                    @Test
                    public void submitIsCalled() {
                        verifySubmitCall(0.0);
                    }

                    @Test
                    public void openPriceIsFilled() {
                        assertThat(tradeParams[2], equalTo(openPrice));
                    }
                }

                public class WhenNormalBuy {

                    @Before
                    public void setUp() {
                        openTradeResult = callOpenTrade();
                    }

                    @Test
                    public void returnValueIsTradeId() {
                        assertThat(openTradeResult, equalTo(tradeId));
                    }

                    @Test
                    public void submitIsCalled() {
                        verifySubmitCall(slPrice);
                    }

                    @Test
                    public void openPriceIsFilled() {
                        assertThat(tradeParams[2], equalTo(openPrice));
                    }
                }
            }
        }
    }
}
