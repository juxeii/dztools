package com.jforex.dzjforex.brokerstop.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerstop.BrokerStop;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Single;

@RunWith(HierarchicalContextRunner.class)
public class BrokerStopTest extends CommonUtilForTest {

    private BrokerStop brokerStop;

    @Mock
    private TaskParamsRunner taskParamsRunnerMock;
    private int nTradeID;

    @Before
    public void setUp() {
        nTradeID = brokerSellData.nTradeID();

        brokerStop = new BrokerStop(taskParamsRunnerMock, tradeUtilityMock);
    }

    private void assertBrokerStopResult(final ZorroReturnValues returnValue) {
        assertThat(brokerStop.setSL(brokerStopData), equalTo(returnValue.getValue()));
    }

    @Test
    public void setSLFailsWhenOrderForTradingThrows() {
        when(tradeUtilityMock.orderForTrading(nTradeID))
            .thenReturn(Single.error(jfException));

        assertBrokerStopResult(ZorroReturnValues.ADJUST_SL_FAIL);
    }

    public class OnValidOrderForSetSL {

        @Before
        public void setUp() {
            when(tradeUtilityMock.orderForTrading(nTradeID))
                .thenReturn(Single.just(orderMock));
        }

        private void setParamsRunnerResult(final Completable result) {
            when(taskParamsRunnerMock.startSetSL(orderMock, brokerStopData))
                .thenReturn(result);
        }

        @Test
        public void setSLFailsWhenParamsRunnerFails() {
            setParamsRunnerResult(Completable.error(jfException));

            assertBrokerStopResult(ZorroReturnValues.ADJUST_SL_FAIL);
        }

        @Test
        public void setSLIsOKWhenParamsRunnerReturnsSucceeds() {
            setParamsRunnerResult(Completable.complete());

            assertBrokerStopResult(ZorroReturnValues.ADJUST_SL_OK);
        }
    }
}
