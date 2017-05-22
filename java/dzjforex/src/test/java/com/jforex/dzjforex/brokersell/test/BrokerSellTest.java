package com.jforex.dzjforex.brokersell.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokersell.BrokerSell;
import com.jforex.dzjforex.brokersell.CloseParamsRunner;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerSellTest extends CommonUtilForTest {

    private BrokerSell brokerSell;

    @Mock
    private CloseParamsRunner closeParamsRunnerMock;
    private int nTradeID;

    @Before
    public void setUp() {
        nTradeID = brokerSellData.orderID();

        brokerSell = new BrokerSell(closeParamsRunnerMock, tradeUtilityMock);
    }

    private TestObserver<Integer> subscribe() {
        return brokerSell
            .closeTrade(brokerSellData)
            .test();
    }

    @Test
    public void sellFailsWhenOrderForTradingThrows() {
        when(tradeUtilityMock.orderForTrading(nTradeID))
            .thenReturn(Single.error(jfException));

        subscribe().assertValue(ZorroReturnValues.BROKER_SELL_FAIL.getValue());
    }

    public class OnValidOrderForSell {

        @Before
        public void setUp() {
            when(tradeUtilityMock.orderForTrading(nTradeID))
                .thenReturn(Single.just(orderMockA));
        }

        private void setParamsRunnerResult(final Completable result) {
            when(closeParamsRunnerMock.get(orderMockA, brokerSellData))
                .thenReturn(result);
        }

        @Test
        public void sellFailsWhenParamsRunnerFails() {
            setParamsRunnerResult(Completable.error(jfException));

            subscribe().assertValue(ZorroReturnValues.BROKER_SELL_FAIL.getValue());
        }

        @Test
        public void whenParamsRunnerSucceedsTheTradeIDIsReturned() {
            setParamsRunnerResult(Completable.complete());

            subscribe().assertValue(nTradeID);
        }
    }
}
