package com.jforex.dzjforex.brokertrade.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.brokertrade.BrokerTrade;
import com.jforex.dzjforex.brokertrade.BrokerTradeData;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.math.CalculationUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerTradeTest extends CommonUtilForTest {

    private BrokerTrade brokerTrade;

    @Mock
    private CalculationUtil calculationUtilMock;
    @Mock
    private BrokerTradeData brokerTradeDataMock;
    private final int orderID = 42;
    private final double price = 1.1203;
    private final double orderAmount = 0.12;
    private final int orderContracts = 120000;
    private final double pOpen = 1.1203;
    private final double pRoll = 0.0;
    private final double pProfit = 23.45;

    @Before
    public void setUp() {
        setUpMocks();

        brokerTrade = new BrokerTrade(tradeUtilityMock, calculationUtilMock);
    }

    private void setUpMocks() {
        when(brokerTradeDataMock.orderID()).thenReturn(orderID);

        when(calculationUtilMock.currentQuoteForOrderCommand(instrumentForTest, OrderCommand.BUY)).thenReturn(price);
        when(tradeUtilityMock.amountToContracts(orderAmount)).thenReturn(orderContracts);

        when(orderMockA.getInstrument()).thenReturn(instrumentForTest);
        when(orderMockA.getOpenPrice()).thenReturn(pOpen);
        when(orderMockA.getAmount()).thenReturn(orderAmount);
        when(orderMockA.getProfitLossInAccountCurrency()).thenReturn(pProfit);
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

        private void verifyFillCallWithCorrectCloseValue(final double pClose) {
            verify(brokerTradeDataMock).fill(pOpen,
                                             pClose,
                                             pRoll,
                                             pProfit);
        }

        @Test
        public void fillCallIsCorrect() {
            when(orderMockA.getOrderCommand()).thenReturn(OrderCommand.BUY);

            subscribe();

            verifyFillCallWithCorrectCloseValue(price);
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
