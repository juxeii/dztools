package com.jforex.dzjforex.brokerbuy.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerbuy.BrokerBuy;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerbuy.SubmitParamsRunner;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerBuyTest extends CommonUtilForTest {

    private BrokerBuy brokerBuy;

    @Mock
    private SubmitParamsRunner submitParamsRunnerMock;
    @Mock
    private OrderRepository orderRepositoryMock;
    @Mock
    private BrokerBuyData brokerBuyDataMock;

    @Before
    public void setUp() {
        setUpMocks();

        brokerBuy = new BrokerBuy(submitParamsRunnerMock,
                                  orderRepositoryMock,
                                  tradeUtilityMock);
    }

    private void setUpMocks() {
        when(brokerBuyDataMock.assetName()).thenReturn(instrumentNameForTest);
        when(brokerBuyDataMock.orderID()).thenReturn(orderID);

        when(orderRepositoryMock.store(orderMockA)).thenReturn(Completable.complete());
    }

    private TestObserver<Integer> subscribe() {
        return brokerBuy
            .openTrade(brokerBuyDataMock)
            .test();
    }

    private OngoingStubbing<Single<Instrument>> stubInstrumentForTrading() {
        return when(tradeUtilityMock.instrumentForTrading(instrumentNameForTest));
    }

    @Test
    public void submitCallIsDeferred() {
        brokerBuy.openTrade(brokerBuyDataMock);

        verifyZeroInteractions(submitParamsRunnerMock);
        verifyZeroInteractions(orderRepositoryMock);
    }

    @Test
    public void submitFailsWhenInstrumentIsNotAvailable() {
        stubInstrumentForTrading().thenReturn(Single.error(jfException));

        subscribe().assertValue(ZorroReturnValues.BROKER_BUY_FAIL.getValue());
    }

    public class OnInstrumentAvailable {

        @Before
        public void setUp() {
            stubInstrumentForTrading().thenReturn(Single.just(instrumentForTest));
        }

        private OngoingStubbing<Single<IOrder>> stubParamsRunner() {
            return when(submitParamsRunnerMock.get(instrumentForTest, brokerBuyDataMock));
        }

        @Test
        public void submitFailsWhenParamsRunnerFails() {
            stubParamsRunner().thenReturn(Single.error(jfException));

            subscribe().assertValue(ZorroReturnValues.BROKER_BUY_FAIL.getValue());
        }

        public class OnParamsRunnerSuccess {

            @Before
            public void setUp() {
                stubParamsRunner().thenReturn(Single.just(orderMockA));
            }

            private OngoingStubbing<Double> stubSLDistance() {
                return when(brokerBuyDataMock.slDistance());
            }

            @Test
            public void whenStopDistanceIsNegativOppositeCloseIsReturned() {
                stubSLDistance().thenReturn(-1.0);

                subscribe().assertValue(ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue());
            }

            @Test
            public void whenStopDistanceIsPositiveTheOrderIDIsReturned() {
                stubSLDistance().thenReturn(0.0034);

                subscribe().assertValue(orderID);
            }
        }
    }
}
