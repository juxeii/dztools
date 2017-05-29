package com.jforex.dzjforex.order.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.math.MathUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class StopLossTest extends CommonUtilForTest {

    private StopLoss stopLoss;

    @Mock
    private CalculationUtil calculationUtilMock;
    @Mock
    private BrokerBuyData brokerBuyDataMock;
    private final OrderCommand orderCommand = OrderCommand.BUY;
    private final double dStopDist = 8.5;
    private final double minPipsForSL = 5.6;
    private final double noStopLossPrice = 0.0;
    private final double oppositeClose = -1;

    @Before
    public void setUp() {
        stopLoss = new StopLoss(calculationUtilMock, minPipsForSL);
    }

    public class ForSubmit {

        @Before
        public void setUp() {
            when(brokerBuyDataMock.dStopDist()).thenReturn(dStopDist);
            when(brokerBuyDataMock.orderCommand()).thenReturn(orderCommand);
        }

        private TestObserver<Double> subscribe() {
            return stopLoss
                .forSubmit(instrumentForTest, brokerBuyDataMock)
                .test();
        }

        private void stubStopDistanceInPips(final double pipDistance) {
            final double dStopDist = InstrumentUtil.scalePipsToPrice(instrumentForTest, pipDistance);
            when(brokerBuyDataMock.dStopDist()).thenReturn(dStopDist);
        }

        @Test
        public void callIsDeferred() {
            stopLoss.forSubmit(instrumentForTest, brokerBuyDataMock);

            verifyZeroInteractions(calculationUtilMock);
            verifyZeroInteractions(brokerBuyDataMock);
        }

        @Test
        public void whenDistanceIsTooSmallErrorIsPropagated() {
            stubStopDistanceInPips(5.5);

            subscribe().assertError(Exception.class);
        }

        @Test
        public void whenDistanceIsZeroNoSLIsReturned() {
            stubStopDistanceInPips(0.0);

            subscribe().assertValue(noStopLossPrice);
        }

        @Test
        public void whenDistanceIsForOppositeCloseNoSLIsReturned() {
            when(brokerBuyDataMock.dStopDist()).thenReturn(oppositeClose);

            subscribe().assertValue(noStopLossPrice);
        }

        public class WhenDistanceIsValid {

            private final double slFromCalculcation = 1.12349;

            private OngoingStubbing<Double> stubSLCalculation(final double dStopDist) {
                return when(calculationUtilMock.slPriceForPips(instrumentForTest,
                                                               orderCommand,
                                                               dStopDist));
            }

            @Test
            public void whenDistanceEqualsMinPipsSLIsReturned() {
                stubStopDistanceInPips(minPipsForSL);
                stubSLCalculation(minPipsForSL).thenReturn(slFromCalculcation);

                subscribe().assertValue(slFromCalculcation);
            }

            @Test
            public void whenDistanceIsBiggerMinPipsSLIsReturned() {
                stubStopDistanceInPips(minPipsForSL + 0.2);
                stubSLCalculation(minPipsForSL + 0.2).thenReturn(slFromCalculcation);

                subscribe().assertValue(slFromCalculcation);
            }
        }
    }

    @Test
    public void slPriceIsRoundedForSetSL() {
        final double slPrice = 1.1234997564;
        final double priceForMock = InstrumentUtil.addPipsToPrice(instrumentForTest,
                                                                  slPrice,
                                                                  25.0);
        when(calculationUtilMock.currentQuoteForOrderCommand(instrumentForTest, orderCommand))
            .thenReturn(priceForMock);

        stopLoss
            .forSetSL(orderMockA, slPrice)
            .test()
            .assertValue(MathUtil.roundPrice(slPrice, instrumentForTest))
            .assertComplete();
    }

    public class ForSetSL {

        private final double slPrice = 1.12349;

        @Before
        public void setUp() {
            when(orderMockA.getOrderCommand()).thenReturn(orderCommand);
        }

        private OngoingStubbing<Double> stubCurrentPrice() {
            return when(calculationUtilMock.currentQuoteForOrderCommand(instrumentForTest, orderCommand));
        }

        private void assertSLWithPipDistance(final double pipDistance) {
            final double priceForMock = InstrumentUtil.addPipsToPrice(instrumentForTest,
                                                                      slPrice,
                                                                      pipDistance);
            stubCurrentPrice().thenReturn(priceForMock);

            stopLoss
                .forSetSL(orderMockA, slPrice)
                .test()
                .assertValue(slPrice)
                .assertComplete();
        }

        @Test
        public void whenDistanceIsTooSmallErrorIsPropagated() {
            stubCurrentPrice().thenReturn(1.12345);

            stopLoss
                .forSetSL(orderMockA, slPrice)
                .test()
                .assertError(Exception.class);
        }

        @Test
        public void whenDistanceEqualsMinPipsSLIsReturned() {
            assertSLWithPipDistance(minPipsForSL);
        }

        @Test
        public void whenDistanceIsBiggerMinPipsSLIsReturned() {
            assertSLWithPipDistance(minPipsForSL + 0.1);
        }
    }
}
