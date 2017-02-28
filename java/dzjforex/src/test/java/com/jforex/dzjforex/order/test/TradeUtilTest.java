package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.misc.AccountInfo;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.dzjforex.test.util.CommonOrderForTest;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class TradeUtilTest extends CommonOrderForTest {

    private TradeUtil tradeUtil;

    @Mock
    private OrderRepository orderRepositoryMock;
    @Mock
    private StrategyUtil strategyUtilMock;
    @Mock
    private OrderLabelUtil labelUtilMock;
    @Mock
    private AccountInfo accountInfoMock;
    @Mock
    private InstrumentUtil instrumentUtilMock;

    private final Instrument testInstrument = Instrument.EURUSD;
    private final int orderID = 42;

    @Before
    public void setUp() {
        when(strategyUtilMock.instrumentUtil(testInstrument))
            .thenReturn(instrumentUtilMock);

        tradeUtil = new TradeUtil(orderRepositoryMock,
                                  strategyUtilMock,
                                  accountInfoMock,
                                  labelUtilMock,
                                  pluginConfigMock);
    }

    @Test
    public void orderByIDReturnsOrderFromRepository() {
        when(orderRepositoryMock.orderByID(orderID)).thenReturn(orderMock);

        final IOrder foundOrder = tradeUtil.orderByID(orderID);

        assertThat(foundOrder, equalTo(orderMock));
        verify(orderRepositoryMock).orderByID(orderID);
    }

    @Test
    public void orderCommandForContractsIsBuyForPositiveContracts() {
        final OrderCommand command = tradeUtil.orderCommandForContracts(1200);

        assertThat(command, equalTo(OrderCommand.BUY));
    }

    @Test
    public void orderCommandForContractsIsSellForNegativeContracts() {
        final OrderCommand command = tradeUtil.orderCommandForContracts(-1200);

        assertThat(command, equalTo(OrderCommand.SELL));
    }

    @Test
    public void amountToContractsIsCorrect() {
        final int contracts = tradeUtil.amountToContracts(0.12);

        assertThat(contracts, equalTo(120000));
    }

    @Test
    public void contractsToAmountIsCorrectForPositiveContracts() {
        final double amount = tradeUtil.contractsToAmount(125000);

        assertThat(amount, equalTo(0.125));
    }

    @Test
    public void contractsToAmountIsCorrectForNegativedContracts() {
        final double amount = tradeUtil.contractsToAmount(-129000);

        assertThat(amount, equalTo(0.129));
    }

    @Test
    public void isSLPriceDistanceOKIsFalseWhenSLIsTooNarrow() {
        final double askPrice = 1.32435;
        final double slPrice = 1.3241;

        when(instrumentUtilMock.askQuote()).thenReturn(askPrice);

        assertFalse(tradeUtil.isSLPriceDistanceOK(testInstrument, slPrice));
    }

    @Test
    public void isSLPriceDistanceOKIsTrueWhenSLIsBigEnough() {
        final double askPrice = 1.32435;
        final double slPrice = 1.32535;

        when(instrumentUtilMock.askQuote()).thenReturn(askPrice);

        assertTrue(tradeUtil.isSLPriceDistanceOK(testInstrument, slPrice));
    }
}
