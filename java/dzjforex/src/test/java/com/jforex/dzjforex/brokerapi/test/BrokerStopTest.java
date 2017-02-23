package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerapi.BrokerStop;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderSetSL;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.math.MathUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerStopTest extends CommonUtilForTest {

    private BrokerStop brokerStop;

    @Mock
    private OrderSetSL orderSetSLMock;
    @Mock
    private IOrder orderMock;
    private final Instrument tradeInstrument = Instrument.EURUSD;
    private int setSLReturnValue;
    private final int nTradeID = 1;
    private final double dStop = 0.0015;

    @Before
    public void setUp() {
        brokerStop = new BrokerStop(orderSetSLMock, tradeUtilMock);
    }

    private int callSetSL() {
        return brokerStop.setSL(nTradeID, dStop);
    }

    @Test
    public void whenOrderIdIsUnknownBrokerAdjustSLFails() {
        when(tradeUtilMock.orderByID(nTradeID)).thenReturn(null);

        assertThat(callSetSL(),
                   equalTo(ZorroReturnValues.ADJUST_SL_FAIL.getValue()));
    }

    public class WhenOrderIdIsKnown {

        @Before
        public void setUp() {
            when(tradeUtilMock.orderByID(nTradeID)).thenReturn(orderMock);
        }

        @Test
        public void whenTradingIsNotAllowedBrokerAdjustSLFails() {
            when(tradeUtilMock.isTradingAllowed()).thenReturn(false);

            assertThat(callSetSL(),
                       equalTo(ZorroReturnValues.ADJUST_SL_FAIL.getValue()));
        }

        public class WhenTradingIsAllowed {

            private final double calculatedSL = MathUtil.roundPrice(dStop, tradeInstrument);

            @Before
            public void setUp() {
                when(orderMock.getInstrument()).thenReturn(tradeInstrument);

                when(tradeUtilMock.isTradingAllowed()).thenReturn(true);
            }

            public class WhenSLDistanceIsTooNarrow {

                @Before
                public void setUp() {
                    when(tradeUtilMock.isSLPriceDistanceOK(tradeInstrument, calculatedSL))
                        .thenReturn(false);

                    setSLReturnValue = callSetSL();
                }

                @Test
                public void returnValueIsNonZeroK() {
                    assertThat(setSLReturnValue,
                               equalTo(ZorroReturnValues.ADJUST_SL_OK.getValue()));
                }

                @Test
                public void noCallToOrderSetSL() {
                    verifyZeroInteractions(orderSetSLMock);
                }
            }

            public class WhenSLDistanceIsFarEnough {

                @Before
                public void setUp() {
                    when(tradeUtilMock.isSLPriceDistanceOK(tradeInstrument, calculatedSL))
                        .thenReturn(true);

                    setSLReturnValue = callSetSL();
                }

                @Test
                public void returnValueIsAdjustSLOK() {
                    assertThat(setSLReturnValue,
                               equalTo(ZorroReturnValues.ADJUST_SL_OK.getValue()));
                }

                @Test
                public void callToOrderSetSLIsCorrect() {
                    verify(orderSetSLMock).run(orderMock, calculatedSL);
                }
            }
        }
    }
}
