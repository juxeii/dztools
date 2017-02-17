package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerLogin;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.ConnectionLostException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;

@RunWith(HierarchicalContextRunner.class)
public class BrokerLoginTest extends CommonUtilForTest {

    private BrokerLogin brokerLogin;

    @Mock
    private ClientUtil clientUtilMock;
    @Mock
    private CredentialsFactory credentialsFactoryMock;
    @Mock
    private IClient clientMock;
    @Mock
    private Authentification authentificationMock;
    private int returnCode;

    @Before
    public void setUp() {
        when(clientUtilMock.client()).thenReturn(clientMock);
        when(clientUtilMock.authentification()).thenReturn(authentificationMock);

        brokerLogin = new BrokerLogin(clientUtilMock, credentialsFactoryMock);
    }

    private void callLogin() {
        returnCode = brokerLogin.doLogin(username,
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
            verifyZeroInteractions(credentialsFactoryMock);
            verifyZeroInteractions(authentificationMock);
        }

        @Test
        public void returnValueIsLoginOK() {
            assertThat(returnCode, equalTo(ReturnCodes.LOGIN_OK));
        }
    }

    @Test
    public void logoutCallsAuthentification() {
        when(authentificationMock.logout())
            .thenReturn(Completable.complete());

        brokerLogin.doLogout();

        verify(authentificationMock).logout();
    }

    public class WhenClientIsDisconnected {

        @Before
        public void setUp() {
            when(clientMock.isConnected()).thenReturn(false);

            when(credentialsFactoryMock.create(username,
                                               password,
                                               loginTypeDemo))
                                                   .thenReturn(loginCredentials);
        }

        public class WhenAuthentificationIsOK {

            @Before
            public void setUp() {
                when(authentificationMock.login(loginCredentials))
                    .thenReturn(Completable.complete());

                callLogin();
            }

            @Test
            public void returnValueIsLoginOK() {
                assertThat(returnCode, equalTo(ReturnCodes.LOGIN_OK));
            }

            @Test
            public void authentificationIsCalledWithCredentials() {
                verify(authentificationMock).login(loginCredentials);
            }
        }

        public class WhenAuthentificationFailes {

            @Before
            public void setUp() {
                when(authentificationMock.login(loginCredentials))
                    .thenReturn(Completable.error(new ConnectionLostException("Login fail!")));

                callLogin();
            }

            @Test
            public void returnValueIsLoginFAIL() {
                assertThat(returnCode, equalTo(ReturnCodes.LOGIN_FAIL));
            }

            @Test
            public void authentificationIsCalledWithCredentials() {
                verify(authentificationMock).login(loginCredentials);
            }
        }
    }
}
