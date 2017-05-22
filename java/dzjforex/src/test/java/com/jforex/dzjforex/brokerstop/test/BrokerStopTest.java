package com.jforex.dzjforex.brokerstop.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerstop.BrokerStop;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerStopTest extends CommonUtilForTest {

    private BrokerStop brokerStop;

    @Mock
    private SetSLParamsRunner setSLParamsRunnerMock;
    private int nTradeID;

    @Before
    public void setUp() {
        nTradeID = brokerSellData.orderID();

        brokerStop = new BrokerStop(setSLParamsRunnerMock, tradeUtilityMock);
    }

    private TestObserver<Integer> subscribe() {
        return brokerStop
            .setSL(brokerStopData)
            .test();
    }

    @Test
    public void setSLCallIsDeferred() {
        brokerStop.setSL(brokerStopData);

        verifyZeroInteractions(setSLParamsRunnerMock);
        verifyZeroInteractions(tradeUtilityMock);
    }

    @Test
    public void setSLFailsWhenOrderForTradingThrows() {
        when(tradeUtilityMock.orderForTrading(nTradeID))
            .thenReturn(Single.error(jfException));

        subscribe().assertValue(ZorroReturnValues.ADJUST_SL_FAIL.getValue());
    }

    public class OnValidOrderForSetSL {

        @Before
        public void setUp() {
            when(tradeUtilityMock.orderForTrading(nTradeID))
                .thenReturn(Single.just(orderMockA));
        }

        private void setParamsRunnerResult(final Completable result) {
            when(setSLParamsRunnerMock.get(orderMockA, brokerStopData))
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
