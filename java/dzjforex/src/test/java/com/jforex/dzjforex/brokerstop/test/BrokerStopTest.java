package com.jforex.dzjforex.brokerstop.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.brokerstop.BrokerStop;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerStopTest extends CommonUtilForTest {

    private BrokerStop brokerStop;

    @Mock
    private SetSLParamsRunner setSLParamsRunnerMock;

    @Before
    public void setUp() {
        brokerStop = new BrokerStop(setSLParamsRunnerMock, tradeUtilityMock);
    }

    private TestObserver<Integer> subscribe() {
        return brokerStop
            .setSL(brokerStopDataMock)
            .test();
    }

    private OngoingStubbing<Single<IOrder>> stubOrderForTrading() {
        return when(tradeUtilityMock.orderForTrading(orderID));
    }

    @Test
    public void setSLCallIsDeferred() {
        brokerStop.setSL(brokerStopDataMock);

        verifyZeroInteractions(setSLParamsRunnerMock);
        verifyZeroInteractions(tradeUtilityMock);
    }

    @Test
    public void setSLFailsWhenOrderForTradingThrows() {
        stubOrderForTrading().thenReturn(Single.error(jfException));

        subscribe().assertValue(ZorroReturnValues.ADJUST_SL_FAIL.getValue());
    }

    public class OnValidOrderForSetSL {

        @Before
        public void setUp() {
            stubOrderForTrading().thenReturn(Single.just(orderMockA));
        }

        private void setParamsRunnerResult(final Completable result) {
            when(setSLParamsRunnerMock.get(orderMockA, brokerStopDataMock))
                .thenReturn(result);
        }

        @Test
        public void setSLFailsWhenParamsRunnerFails() {
            setParamsRunnerResult(Completable.error(jfException));

            subscribe().assertValue(ZorroReturnValues.ADJUST_SL_FAIL.getValue());
        }

        @Test
        public void setSLIsOKWhenParamsRunnerReturnsSucceeds() {
            setParamsRunnerResult(Completable.complete());

            subscribe().assertValue(ZorroReturnValues.ADJUST_SL_OK.getValue());
        }
    }
}
