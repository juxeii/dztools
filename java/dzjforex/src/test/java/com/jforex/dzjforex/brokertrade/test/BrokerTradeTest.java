package com.jforex.dzjforex.brokertrade.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.brokertrade.BrokerTrade;
import com.jforex.dzjforex.brokertrade.BrokerTradeData;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerTradeTest extends CommonUtilForTest {

    private BrokerTrade brokerTrade;

    @Mock
    private BrokerTradeData brokerTradeDataMock;
    private final double orderAmount = 0.12;
    private final int orderContracts = 120000;

    @Before
    public void setUp() {
        setUpMocks();

        brokerTrade = new BrokerTrade(tradeUtilityMock);
    }

    private void setUpMocks() {
        when(orderMockA.getAmount()).thenReturn(orderAmount);

        when(brokerTradeDataMock.orderID()).thenReturn(orderID);

        when(tradeUtilityMock.amountToContracts(orderAmount)).thenReturn(orderContracts);
    }

    private TestObserver<Integer> subscribe() {
        return brokerTrade
            .fillParams(brokerTradeDataMock)
            .test();
    }

    @Test
    public void whenOrderNotAvailableUnknownOrderIsReturned() {
        when(tradeUtilityMock.orderByID(orderID)).thenReturn(Maybe.empty());

        subscribe().assertValue(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue());
    }

    public class WhenOrderAvailable {

        @Before
        public void setUp() {
            when(tradeUtilityMock.orderByID(orderID)).thenReturn(Maybe.just(orderMockA));
        }

        @Test
        public void fillCallIsCorrect() {
            subscribe();

            verify(brokerTradeDataMock).fill(orderMockA);
        }

        @Test
        public void returnValueIsOrderRecentlyClosedWhenOrderIsInStateClosed() {
            when(orderMockA.getState()).thenReturn(IOrder.State.CLOSED);

            subscribe().assertValue(ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue());
        }

        @Test
        public void returnValueIsOrderContractsWhenOrderIsOpen() {
            when(orderMockA.getState()).thenReturn(IOrder.State.OPENED);

            subscribe().assertValue(orderContracts);
        }
    }
}
