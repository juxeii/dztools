package com.jforex.dzjforex.test.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.Clock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;
import org.mockito.Mock;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.Components;
import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.brokersell.BrokerSellData;
import com.jforex.dzjforex.brokerstop.BrokerStopData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderLookup;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.TaskParams;
import com.jforex.programming.strategy.StrategyUtil;

public class CommonUtilForTest extends BDDMockito {

    @Mock
    protected IClient clientMock;
    @Mock
    protected AccountInfo accountInfoMock;
    @Mock
    protected Components componentsMock;
    @Mock
    protected Zorro zorroMock;
    @Mock
    protected IEngine engineMock;
    @Mock
    protected TradeUtility tradeUtilityMock;
    @Mock
    protected RetryParams retryParamsMock;
    @Mock
    protected IOrder orderMockA;
    @Mock
    protected IOrder orderMockB;
    @Mock
    protected OrderUtil orderUtilMock;
    @Mock
    protected InfoStrategy infoStrategyMock;
    @Mock
    protected StrategyUtil strategyUtilMock;
    @Mock
    protected OrderLookup orderLookupMock;
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
    protected static final String orderLabel = orderLabelPrefix + "12345";

    protected static final LoginCredentials loginCredentials =
            new LoginCredentials(jnlpDEMO,
                                 username,
                                 password);
    protected static final LoginCredentials loginCredentialsWithPin =
            new LoginCredentials(jnlpReal,
                                 username,
                                 password,
                                 pin);
    protected final OrderEvent orderEventA;
    protected final OrderEvent orderEventB;
    private final int nTradeID = 42;
    private final int nAmount = 7;
    private final double dStopDist = 0.0035;
    protected BrokerSellData brokerSellData = new BrokerSellData(nTradeID, nAmount);

    private final double slPrice = 1.09875;
    protected BrokerStopData brokerStopData = new BrokerStopData(nTradeID, slPrice);

    private final double tradeParams[] = new double[1];
    protected String instrumentNameForTest = "EUR/USD";
    protected Instrument instrumentForTest = Instrument.EURUSD;
    protected ICurrency baseCurrencyForTest = instrumentForTest.getPrimaryJFCurrency();
    protected BrokerBuyData brokerBuyData;

    private final String accounts[] = new String[1];
    protected BrokerLoginData brokerLoginData = new BrokerLoginData(username,
                                                                    password,
                                                                    loginTypeDemo,
                                                                    accounts);

    protected static final RxTestUtil rxTestUtil = RxTestUtil.get();
    protected static final JFException jfException = new JFException("");
    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    public CommonUtilForTest() {
        initMocks(this);

        when(componentsMock.zorro()).thenReturn(zorroMock);
        when(componentsMock.pluginConfig()).thenReturn(pluginConfigMock);

        when(infoStrategyMock.strategyUtil()).thenReturn(strategyUtilMock);

        when(tradeUtilityMock.orderLabelUtil()).thenReturn(orderLabelUtilMock);
        when(tradeUtilityMock.retryParams()).thenReturn(retryParamsMock);

        when(pluginConfigMock.orderLabelPrefix()).thenReturn(orderLabelPrefix);
        when(pluginConfigMock.lotScale()).thenReturn(1000000.0);
        when(pluginConfigMock.minPipsForSL()).thenReturn(10.0);
        when(pluginConfigMock.tickFetchMillis()).thenReturn(30L);
        when(pluginConfigMock.demoConnectURL()).thenReturn(jnlpDEMO);
        when(pluginConfigMock.realConnectURL()).thenReturn(jnlpReal);
        when(pluginConfigMock.demoLoginType()).thenReturn(loginTypeDemo);
        when(pluginConfigMock.realLoginType()).thenReturn(loginTypeReal);

        orderEventA = new OrderEvent(orderMockA,
                                     OrderEventType.SUBMIT_OK,
                                     true);
        orderEventB = new OrderEvent(orderMockB,
                                     OrderEventType.FULLY_FILLED,
                                     true);

        brokerBuyData = new BrokerBuyData(instrumentNameForTest,
                                          nAmount,
                                          dStopDist,
                                          tradeParams,
                                          pluginConfigMock);

        coverageOnEnumsCorrection();
    }

    private final void coverageOnEnumsCorrection() {
        ZorroReturnValues
            .valueOf(ZorroReturnValues.ACCOUNT_AVAILABLE.toString());
    }

    public void assertComposeParamsForTask(final TaskParams taskParams) throws Exception {
        final ComposeData composeData = taskParams.composeData();

        assertThat(composeData.retryParams(), equalTo(retryParamsMock));

        composeData.completeAction().run();
        composeData.startAction().run();
        composeData.errorConsumer().accept(jfException);
    }
}
