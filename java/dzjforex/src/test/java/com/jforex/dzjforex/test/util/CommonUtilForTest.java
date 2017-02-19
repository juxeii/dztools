package com.jforex.dzjforex.test.util;

import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;

import com.dukascopy.api.ICurrency;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.currency.CurrencyFactory;

public class CommonUtilForTest extends BDDMockito {

    protected static final String jnlpDEMO = "jnlpDEMO";
    protected static final String jnlpReal = "jnlpReal";
    protected static final String username = "John";
    protected static final String password = "Doe123";
    protected static final String pin = "1234";
    protected static final String loginTypeDemo = "Demo";
    protected static final String loginTypeReal = "Real";
    protected static final ICurrency accountCurrency = CurrencyFactory.EUR;

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
    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    public CommonUtilForTest() {
        initMocks(this);
    }
}
