package com.jforex.dzjforex.brokerlogin.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerlogin.BrokerLogin;
import com.jforex.dzjforex.brokerlogin.CredentialsFactory;
import com.jforex.dzjforex.brokerlogin.LoginRetryTimer;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.connection.Authentification;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerLoginTest extends CommonUtilForTest {

    private BrokerLogin brokerLogin;

    @Mock
    private Authentification authentificationMock;
    @Mock
    private CredentialsFactory credentialsFactoryMock;
    @Mock
    private LoginRetryTimer loginRetryTimerMock;

    @Before
    public void setUp() {
        setUpMocks();

        brokerLogin = new BrokerLogin(authentificationMock,
                                      credentialsFactoryMock,
                                      loginRetryTimerMock);
    }

    private void setUpMocks() {
        when(credentialsFactoryMock.create(brokerLoginData)).thenReturn(loginCredentials);

        when(authentificationMock.logout()).thenReturn(Completable.complete());
    }

    private void setLoginRetryTimerPermission(final boolean isLoginPermitted) {
        when(loginRetryTimerMock.isLoginPermitted()).thenReturn(isLoginPermitted);
    }

    private TestObserver<Integer> subscribeLogin() {
        return brokerLogin
            .login(brokerLoginData)
            .test();
    }

    private void verifyNoMockInteractions() {
        verifyZeroInteractions(authentificationMock);
        verifyZeroInteractions(credentialsFactoryMock);
        verifyZeroInteractions(loginRetryTimerMock);
    }

    @Test
    public void loginIsDeferred() {
        brokerLogin.login(brokerLoginData);

        verifyNoMockInteractions();
    }

    @Test
    public void logoutIsDeferred() {
        brokerLogin.logout();

        verifyNoMockInteractions();
    }

    @Test
    public void logoutReturnsLogoutOK() {
        brokerLogin
            .logout()
            .test()
            .assertValue(ZorroReturnValues.LOGOUT_OK.getValue());
    }

    @Test
    public void whenLoginRetryTimerRunsTheLoginFails() {
        setLoginRetryTimerPermission(false);

        subscribeLogin().assertValue(ZorroReturnValues.LOGIN_FAIL.getValue());
    }

    public class WhenLoginRetryTimerNotStarted {

        @Before
        public void setUp() {
            setLoginRetryTimerPermission(true);
        }

        private void setAuthentificationResultOnLogin(final Completable loginResult) {
            when(authentificationMock.login(loginCredentials)).thenReturn(loginResult);
        }

        public class WhenAuthentificationFails {

            @Before
            public void setUp() {
                setAuthentificationResultOnLogin(Completable.error(jfException));
            }

            @Test
            public void returnValueIsLoginFail() {
                subscribeLogin().assertValue(ZorroReturnValues.LOGIN_FAIL.getValue());
            }

            @Test
            public void retryTimerIsStarted() {
                subscribeLogin();

                verify(loginRetryTimerMock).start();
            }
        }

        public class WhenAuthentificationSucceeds {

            @Before
            public void setUp() {
                setAuthentificationResultOnLogin(Completable.complete());
            }

            @Test
            public void returnValueIsLoginOK() {
                subscribeLogin().assertValue(ZorroReturnValues.LOGIN_OK.getValue());
            }

            @Test
            public void retryTimerIsNotStarted() {
                subscribeLogin();

                verify(loginRetryTimerMock, never()).start();
            }
        }
    }
}
