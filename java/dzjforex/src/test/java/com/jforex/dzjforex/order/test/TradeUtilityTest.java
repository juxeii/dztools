package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.order.OrderLookup;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Maybe;

@RunWith(HierarchicalContextRunner.class)
public class TradeUtilityTest extends CommonUtilForTest {

    private TradeUtility tradeUtility;

    @Mock
    private OrderLookup orderLookupMock;
    @Mock
    private HistoryWrapper historyWrapperMock;
    private final int orderID = 42;

    @Before
    public void setUp() {
        tradeUtility = new TradeUtility(orderLookupMock,
                                        accountInfoMock,
                                        orderLabelUtilMock,
                                        pluginConfigMock);
    }

    private OngoingStubbing<Boolean> stubIsTradingAllowed() {
        return when(accountInfoMock.isTradingAllowed());
    }

    private OngoingStubbing<Maybe<IOrder>> stubOrderByID() {
        return when(orderLookupMock.getByID(orderID));
    }

    @Test
    public void assertOrderLabelUtil() {
        assertNotNull(tradeUtility.orderLabelUtil());
    }

    @Test
    public void amountToContractsIsCorrect() {
        final double amount = 0.125;

        final int contracts = tradeUtility.amountToContracts(amount);

        assertThat(contracts, equalTo(125000));
    }

    @Test
    public void positiveContractsToAmountIsCorrect() {
        final int contracts = 125000;

        final double amount = tradeUtility.contractsToAmount(contracts);

        assertThat(amount, equalTo(0.125));
    }

    @Test
    public void negativeContractsToAmountIsCorrect() {
        final int contracts = -125000;

        final double amount = tradeUtility.contractsToAmount(contracts);

        assertThat(amount, equalTo(0.125));
    }

    @Test
    public void orderCommandForPositiveContractsIsBuy() {
        final int contracts = 125000;

        final OrderCommand command = tradeUtility.orderCommandForContracts(contracts);

        assertThat(command, equalTo(OrderCommand.BUY));
    }

    @Test
    public void orderCommandForNegativeContractsIsSell() {
        final int contracts = -125000;

        final OrderCommand command = tradeUtility.orderCommandForContracts(contracts);

        assertThat(command, equalTo(OrderCommand.SELL));
    }

    @Test
    public void orderByIDReturnsValueFromOrderLookup() {
        stubOrderByID().thenReturn(Maybe.just(orderMockA));

        final IOrder order = tradeUtility
            .orderByID(orderID)
            .blockingGet();

        assertThat(order, equalTo(orderMockA));
    }

    public class WhenTradingIsNotAllowed {

        @Before
        public void setUp() {
            stubIsTradingAllowed().thenReturn(false);
        }

        @Test
        public void instrumentForTradingFails() {
            tradeUtility
                .instrumentForTrading(instrumentNameForTest)
                .test()
                .assertError(Exception.class);
        }

        @Test
        public void orderForTradingFails() {
            tradeUtility
                .orderForTrading(orderID)
                .test()
                .assertError(Exception.class);
        }
    }

    public class WhenTradingIsAllowed {

        @Before
        public void setUp() {
            stubIsTradingAllowed().thenReturn(true);

            stubOrderByID().thenReturn(Maybe.just(orderMockA));
        }

        @Test
        public void instrumentForTradingSucceeds() {
            tradeUtility
                .instrumentForTrading(instrumentNameForTest)
                .test()
                .assertValue(instrumentForTest)
                .assertComplete();
        }

        @Test
        public void orderForTradingSucceeds() {
            tradeUtility
                .orderForTrading(orderID)
                .test()
                .assertValue(orderMockA)
                .assertComplete();
        }
    }
}
