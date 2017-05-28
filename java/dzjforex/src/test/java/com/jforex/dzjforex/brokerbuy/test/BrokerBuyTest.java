package com.jforex.dzjforex.brokerbuy.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

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
import io.reactivex.Maybe;
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
    private final int orderID = 42;

    @Before
    public void setUp() {
        setUpMocks();

        brokerBuy = new BrokerBuy(submitParamsRunnerMock,
                                  orderRepositoryMock,
                                  tradeUtilityMock);
    }

    private void setUpMocks() {
        when(brokerBuyDataMock.instrumentName()).thenReturn(instrumentNameForTest);

        when(orderLabelUtilMock.idFromOrder(orderMockA)).thenReturn(Maybe.just(orderID));

        when(orderRepositoryMock.store(orderMockA)).thenReturn(Completable.complete());
    }

    private TestObserver<Integer> subscribe() {
        return brokerBuy
            .openTrade(brokerBuyDataMock)
            .test();
    }

    private void setTradeUtilityResult(final Single<Instrument> result) {
        when(tradeUtilityMock.instrumentForTrading(instrumentNameForTest))
            .thenReturn(result);
    }

    @Test
    public void submitCallIsDeferred() {
        brokerBuy.openTrade(brokerBuyDataMock);

        verifyZeroInteractions(submitParamsRunnerMock);
        verifyZeroInteractions(orderRepositoryMock);
    }

    @Test
    public void submitFailsWhenInstrumentIsNotAvailable() {
        setTradeUtilityResult(Single.error(jfException));

        subscribe().assertValue(ZorroReturnValues.BROKER_BUY_FAIL.getValue());
    }

    public class OnInstrumentAvailable {

        @Before
        public void setUp() {
            setTradeUtilityResult(Single.just(instrumentForTest));
        }

        private void setParamsRunnerResult(final Single<IOrder> result) {
            when(submitParamsRunnerMock.get(instrumentForTest, brokerBuyDataMock))
                .thenReturn(result);
        }

        @Test
        public void submitFailsWhenParamsRunnerFails() {
            setParamsRunnerResult(Single.error(jfException));

            subscribe().assertValue(ZorroReturnValues.BROKER_BUY_FAIL.getValue());
        }

        public class OnParamsRunnerSuccess {

            @Before
            public void setUp() {
                setParamsRunnerResult(Single.just(orderMockA));
            }

            @Test
            public void whenStopDistanceIsNegativOppositeCloseIsReturned() {
                when(brokerBuyDataMock.dStopDist()).thenReturn(-1.0);

                subscribe().assertValue(ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue());
            }

            @Test
            public void whenStopDistanceIsPositiveTheOrderIDIsReturned() {
                when(brokerBuyDataMock.dStopDist()).thenReturn(0.0034);

                subscribe().assertValue(orderID);
            }
        }
    }
}
