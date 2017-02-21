package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IAccount.AccountState;
import com.dukascopy.api.ICurrency;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.math.CalculationUtil;

public class AccountInfoTest extends CommonUtilForTest {

    private AccountInfo accountInfo;

    @Mock
    private IAccount accountMock;
    @Mock
    private CalculationUtil calculationUtilMock;
    @Mock
    private PluginConfig pluginConfigMock;

    private static final AccountState state = IAccount.AccountState.OK;
    private static final String id = "1234";
    private static final ICurrency accountCurrency = CurrencyFactory.EUR;
    private static final double equity = 614527.45;
    private static final double basEquity = 614740.45;
    private static final double balance = 54331.78;
    private static final double leverage = 100;
    private static final double creditLine = 556.23;
    private static final double lotSize = 1000;
    private static final boolean isGlobal = false;

    @Before
    public void setUp() {
        when(pluginConfigMock.lotSize()).thenReturn(lotSize);

        when(accountMock.getAccountState()).thenReturn(state);
        when(accountMock.getAccountId()).thenReturn(id);
        when(accountMock.getEquity()).thenReturn(equity);
        when(accountMock.getBaseEquity()).thenReturn(basEquity);
        when(accountMock.getBalance()).thenReturn(balance);
        when(accountMock.getLeverage()).thenReturn(leverage);
        when(accountMock.getCreditLine()).thenReturn(creditLine);
        when(accountMock.isGlobal()).thenReturn(isGlobal);
        when(accountMock.isConnected()).thenReturn(false);
        when(accountMock.getAccountCurrency()).thenReturn(accountCurrency);

        accountInfo = new AccountInfo(accountMock,
                                      calculationUtilMock,
                                      pluginConfigMock);
    }

    @Test
    public void stateIsCorrect() {
        assertThat(accountInfo.state(), equalTo(state));
    }

    @Test
    public void idIsCorrect() {
        assertThat(accountInfo.id(), equalTo(id));
    }

    @Test
    public void equityIsCorrect() {
        assertThat(accountInfo.equity(), equalTo(equity));
    }

    @Test
    public void basEquityIsCorrect() {
        assertThat(accountInfo.baseEquity(), equalTo(basEquity));
    }

    @Test
    public void balanceIsCorrect() {
        assertThat(accountInfo.balance(), equalTo(balance));
    }

    @Test
    public void currencyIsCorrect() {
        assertThat(accountInfo.currency(), equalTo(accountCurrency));
    }

    @Test
    public void lotSizeIsCorrect() {
        assertThat(accountInfo.lotSize(), equalTo(lotSize));
    }

    @Test
    public void lotMarginIsCorrect() {
        assertThat(accountInfo.lotMargin(), equalTo(lotSize / leverage));
    }

    @Test
    public void tradeValueIsCorrect() {
        assertThat(accountInfo.tradeValue(), equalTo(equity - basEquity));
    }

    @Test
    public void freeMarginIsCorrect() {
        assertThat(accountInfo.freeMargin(), equalTo(creditLine / leverage));
    }

    @Test
    public void usedMarginIsCorrect() {
        assertThat(accountInfo.usedMargin(), equalTo(equity - (creditLine / leverage)));
    }

    @Test
    public void leverageIsCorrect() {
        assertThat(accountInfo.leverage(), equalTo(leverage));
    }

    @Test
    public void isConnectedIsCorrect() {
        assertThat(accountInfo.isConnected(), equalTo(false));
    }

    @Test
    public void isNFACompliantIsCorrect() {
        assertThat(accountInfo.isNFACompliant(), equalTo(isGlobal));
    }

    @Test
    public void tradingIsNotAllowedInState() {
        assertTradingAllowed(IAccount.AccountState.OK, true);
        assertTradingAllowed(IAccount.AccountState.OK_NO_MARGIN_CALL, true);

        assertTradingAllowed(IAccount.AccountState.MARGIN_CALL, false);
        assertTradingAllowed(IAccount.AccountState.MARGIN_CLOSING, false);
        assertTradingAllowed(IAccount.AccountState.BLOCKED, false);
        assertTradingAllowed(IAccount.AccountState.DISABLED, false);
    }

    private void assertTradingAllowed(final IAccount.AccountState accountState,
                                      final boolean isExpectedAllowed) {
        when(accountMock.getAccountState()).thenReturn(accountState);

        assertThat(accountInfo.isTradingAllowed(), equalTo(isExpectedAllowed));
    }
}
