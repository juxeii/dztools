package com.jforex.dzjforex.brokersell.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokersell.BrokerSell;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Single;

@RunWith(HierarchicalContextRunner.class)
public class BrokerSellTest extends CommonUtilForTest {

    private BrokerSell brokerSell;

    @Mock
    private TaskParamsRunner taskParamsRunnerMock;
    private int nTradeID;

    @Before
    public void setUp() {
        nTradeID = brokerSellData.nTradeID();

        brokerSell = new BrokerSell(taskParamsRunnerMock, tradeUtilityMock);
    }

    private void assertBrokerSellResult(final int expectedReturnValue) {
        assertThat(brokerSell.closeTrade(brokerSellData), equalTo(expectedReturnValue));
    }

    @Test
    public void sellFailsWhenOrderForTradingThrows() {
        when(tradeUtilityMock.orderForTrading(nTradeID))
            .thenReturn(Single.error(jfException));

        assertBrokerSellResult(ZorroReturnValues.BROKER_SELL_FAIL.getValue());
    }

    public class OnValidOrderForSell {

        @Before
        public void setUp() {
            when(tradeUtilityMock.orderForTrading(nTradeID))
                .thenReturn(Single.just(orderMock));
        }

        private void setParamsRunnerResult(final Completable result) {
            when(taskParamsRunnerMock.startClose(orderMock, brokerSellData))
                .thenReturn(result);
        }

        @Test
        public void sellFailsWhenParamsRunnerFails() {
            setParamsRunnerResult(Completable.error(jfException));

            assertBrokerSellResult(ZorroReturnValues.BROKER_SELL_FAIL.getValue());
        }

        @Test
        public void whenParamsRunnerSucceedsTheTradeIDIsReturned() {
            setParamsRunnerResult(Completable.complete());

            assertBrokerSellResult(nTradeID);
        }
    }
}
