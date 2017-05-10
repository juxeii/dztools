package com.jforex.dzjforex.test.util;

import static org.mockito.MockitoAnnotations.initMocks;

import java.time.Clock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;
import org.mockito.Mock;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.Components;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.strategy.StrategyUtil;

public class CommonUtilForTest extends BDDMockito {

    @Mock
    protected Components componentsMock;
    @Mock
    protected Zorro zorroMock;
    @Mock
    protected IEngine engineMock;
    @Mock
    protected TradeUtility tradeUtilMock;
    @Mock
    protected InfoStrategy infoStrategyMock;
    @Mock
    protected StrategyUtil strategyUtilMock;
    @Mock
    protected OrderRepository orderRepositoryMock;
    @Mock
    protected OrderLabelUtil orderLabelUtilMock;
    @Mock
    protected PluginConfig pluginConfigMock;
    @Mock
    protected Clock clockMock;

    protected static final String jnlpDEMO = "jnlpDEMO";
    protected static final String jnlpReal = "jnlpReal";
    protected static final String username = "John";
    protected static final String password = "Doe123";
    protected static final String pin = "1234";
    protected static final String loginTypeDemo = "Demo";
    protected static final String loginTypeReal = "Real";
    protected static final ICurrency accountCurrency = CurrencyFactory.EUR;
    protected static final String orderLabelPrefix = "Zorro";

    protected static final LoginCredentials loginCredentials =
            new LoginCredentials(jnlpDEMO,
                                 username,
                                 password);
    protected static final LoginCredentials loginCredentialsWithPin =
            new LoginCredentials(jnlpReal,
                                 username,
                                 password,
                                 pin);

    protected static final RxTestUtil rxTestUtil = RxTestUtil.get();
    protected static final JFException jfException = new JFException("");
    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    public CommonUtilForTest() {
        initMocks(this);

        when(componentsMock.zorro()).thenReturn(zorroMock);
        when(componentsMock.pluginConfig()).thenReturn(pluginConfigMock);

        when(infoStrategyMock.strategyUtil()).thenReturn(strategyUtilMock);

        when(tradeUtilMock.orderLabelUtil()).thenReturn(orderLabelUtilMock);

        when(pluginConfigMock.orderLabelPrefix()).thenReturn(orderLabelPrefix);
        when(pluginConfigMock.lotScale()).thenReturn(1000000.0);
        when(pluginConfigMock.minPipsForSL()).thenReturn(10.0);
        when(pluginConfigMock.tickFetchMinutes()).thenReturn(30);

        coverageOnEnumsCorrection();
    }

    private final void coverageOnEnumsCorrection() {
        OrderActionResult
            .valueOf(OrderActionResult.OK.toString());
        OrderActionResult
            .valueOf(OrderActionResult.OK.toString());
        ZorroReturnValues
            .valueOf(ZorroReturnValues.ACCOUNT_AVAILABLE.toString());
    }
}
