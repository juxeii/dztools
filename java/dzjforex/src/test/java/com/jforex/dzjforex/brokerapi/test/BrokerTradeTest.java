package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerapi.BrokerTrade;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.instrument.InstrumentUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerTradeTest extends CommonUtilForTest {

    private BrokerTrade brokerTrade;

    @Mock
    private InstrumentUtil instrumentUtilMock;
    @Mock
    private IOrder orderMock;
    private final double askQuote = 1.23350;
    private final double bidQuote = 1.23345;
    private final double orderPL = 1234.45;
    private final double orderAmount = 0.12;
    private final int noOfContracts = 120000;
    private final double orderOpenPrice = 1.23888;
    private final double pRoll = 0.0;
    private final Instrument tradeInstrument = Instrument.EURUSD;
    private final int nTradeID = 1;
    private final double orderParams[] = new double[4];

    @Before
    public void setUp() {
        brokerTrade = new BrokerTrade(tradeUtilMock);
    }

    private int callFillTradeParams() {
        return brokerTrade.handle(nTradeID, orderParams);
    }

    @Test
    public void whenOrderIdIsUnknownBrokerTradeRetunsUnknownOrderId() {
        when(tradeUtilMock.orderByID(nTradeID)).thenReturn(null);

        assertThat(callFillTradeParams(),
                   equalTo(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue()));
    }

    public class WhenOrderIdIsKnown {

        @Before
        public void setUp() {
            when(tradeUtilMock.orderByID(nTradeID))
                .thenReturn(orderMock);
            when(tradeUtilMock.amountToContracts(orderAmount))
                .thenReturn(noOfContracts);

            when(orderMock.getInstrument())
                .thenReturn(tradeInstrument);
            when(orderMock.getProfitLossInAccountCurrency())
                .thenReturn(orderPL);
            when(orderMock.getAmount())
                .thenReturn(orderAmount);
            when(orderMock.getOpenPrice())
                .thenReturn(orderOpenPrice);

            when(strategyUtilMock.instrumentUtil(tradeInstrument))
                .thenReturn(instrumentUtilMock);

            when(instrumentUtilMock.askQuote())
                .thenReturn(askQuote);
            when(instrumentUtilMock.bidQuote())
                .thenReturn(bidQuote);
        }

        @Test
        public void whenOrderStateIsClosedReturnValueIsRecentlyClosed() {
            when(orderMock.getState()).thenReturn(IOrder.State.CLOSED);

            assertThat(callFillTradeParams(),
                       equalTo(ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue()));
        }

        public class WhenOrderStateIsNotClosed {

            private void assertCommonOrderParamsAreCorrectFilled() {
                assertThat(orderParams[0], equalTo(orderOpenPrice));
                assertThat(orderParams[2], equalTo(pRoll));
                assertThat(orderParams[3], equalTo(orderPL));
            }

            @Before
            public void setUp() {
                when(orderMock.getState()).thenReturn(IOrder.State.FILLED);
            }

            @Test
            public void returnValueIsNoOfContracts() {
                assertThat(callFillTradeParams(), equalTo(noOfContracts));
            }

            public class WhenOrderIsLong {

                @Before
                public void setUp() {
                    when(orderMock.isLong()).thenReturn(true);

                    callFillTradeParams();
                }

                @Test
                public void orderParamsFillIsCorrect() {
                    assertCommonOrderParamsAreCorrectFilled();
                    assertThat(orderParams[1], equalTo(askQuote));
                }
            }

            public class WhenOrderIsShort {

                @Before
                public void setUp() {
                    when(orderMock.isLong()).thenReturn(false);

                    callFillTradeParams();
                }

                @Test
                public void orderParamsFillIsCorrect() {
                    assertCommonOrderParamsAreCorrectFilled();
                    assertThat(orderParams[1], equalTo(bidQuote));
                }
            }
        }
    }
}
