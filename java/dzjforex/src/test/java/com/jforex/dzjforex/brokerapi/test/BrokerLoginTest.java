package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerLogin;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.LoginHandler;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.test.util.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerLoginTest extends CommonUtilForTest {

    private BrokerLogin brokerLogin;

    @Mock
    private LoginHandler loginHandlerMock;
    @Mock
    private IClient clientMock;
    @Mock
    private PluginConfig pluginConfigMock;
    private final long loginRetryDelay = 5000L;
    private int returnCode;

    @Before
    public void setUp() {
        when(pluginConfigMock.loginRetryDelay()).thenReturn(loginRetryDelay);

        brokerLogin = new BrokerLogin(loginHandlerMock,
                                      clientMock,
                                      pluginConfigMock);
    }

    private void callLogin() {
        returnCode = brokerLogin.login(username,
                                       password,
                                       loginTypeDemo);
    }

    public class WhenClientIsConnected {

        @Before
        public void setUp() {
            when(clientMock.isConnected()).thenReturn(true);

            callLogin();
        }

        @Test
        public void loginCallDoesNothing() {
            verifyZeroInteractions(loginHandlerMock);
        }

        @Test
        public void returnValueIsLoginOK() {
            assertThat(returnCode, equalTo(ZorroReturnValues.LOGIN_OK.getValue()));
        }
    }

    @Test
    public void logoutCallsLoginHandler() {
        when(loginHandlerMock.logout()).thenReturn(ZorroReturnValues.LOGOUT_OK.getValue());

        returnCode = brokerLogin.logout();

        assertThat(returnCode, equalTo(ZorroReturnValues.LOGOUT_OK.getValue()));
        verify(loginHandlerMock).logout();
    }

    public class WhenClientIsDisconnected {

        private void setLoginResult(final int loginResult) {
            when(loginHandlerMock.login(username,
                                        password,
                                        loginTypeDemo))
                                            .thenReturn(loginResult);
        }

        private void verifyLoginCall(final int times) {
            verify(loginHandlerMock, times(times)).login(username,
                                                         password,
                                                         loginTypeDemo);
        }

        @Before
        public void setUp() {
            when(clientMock.isConnected()).thenReturn(false);
        }

        public class WhenLoginIsOK {

            @Before
            public void setUp() {
                setLoginResult(ZorroReturnValues.LOGIN_OK.getValue());

                callLogin();
            }

            @Test
            public void returnValueIsLoginOK() {
                assertThat(returnCode, equalTo(ZorroReturnValues.LOGIN_OK.getValue()));
            }

            @Test
            public void loginHandlerIsCalledCorrect() {
                verifyLoginCall(1);
            }
        }

        public class WhenLoginIsOKFailes {

            @Before
            public void setUp() {
                setLoginResult(ZorroReturnValues.LOGIN_FAIL.getValue());

                callLogin();
            }

            @Test
            public void returnValueIsLoginFAIL() {
                assertThat(returnCode, equalTo(ZorroReturnValues.LOGIN_FAIL.getValue()));
            }

            @Test
            public void loginHandlerIsCalledCorrect() {
                verifyLoginCall(1);
            }

            public class WhenRetryLogin {

                @Before
                public void setUp() {
                    setLoginResult(ZorroReturnValues.LOGIN_OK.getValue());
                }

                @Test
                public void withinRetryDelayDoesNotLoginOnHandler() {
                    RxTestUtil.advanceTimeBy(200L, TimeUnit.MILLISECONDS);

                    callLogin();

                    verifyLoginCall(1);
                }

                @Test
                public void afterRetryDelayLoginOnHandlerIsCalledAgain() {
                    RxTestUtil.advanceTimeBy(loginRetryDelay, TimeUnit.MILLISECONDS);

                    callLogin();

                    verifyLoginCall(2);
                }
            }
        }
    }
}
