package com.jforex.dzjforex.brokerlogin.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.brokerlogin.CredentialsFactory;
import com.jforex.dzjforex.brokerlogin.PinProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.connection.LoginCredentials;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class CredentialsFactoryTest extends CommonUtilForTest {

    private CredentialsFactory credentialsFactory;

    @Mock
    private PinProvider pinProviderMock;
    @Mock
    private BrokerLoginData brokerLoginDataMock;
    private LoginCredentials credentials;

    @Before
    public void setUp() {
        setUpMocks();

        credentialsFactory = new CredentialsFactory(pinProviderMock, pluginConfigMock);
    }

    private void setUpMocks() {
        when(pinProviderMock.getPin()).thenReturn(pin);

        when(brokerLoginDataMock.username()).thenReturn(username);
        when(brokerLoginDataMock.password()).thenReturn(password);

        when(pluginConfigMock.demoConnectURL()).thenReturn(jnlpDEMO);
        when(pluginConfigMock.realConnectURL()).thenReturn(jnlpReal);
        when(pluginConfigMock.demoLoginType()).thenReturn(loginTypeDemo);
        when(pluginConfigMock.realLoginType()).thenReturn(loginTypeReal);
    }

    private void assertUserCredentials() {
        assertThat(credentials.username(), equalTo(username));
        assertThat(credentials.password(), equalTo(password));
    }

    private void assertJnlpAddress(final String address) {
        assertThat(credentials.jnlpAddress(), equalTo(address));
    }

    private void setLoginTypeOnData(final String loginType) {
        when(brokerLoginDataMock.loginType()).thenReturn(loginType);
    }

    private void callCreate() {
        credentials = credentialsFactory.create(brokerLoginDataMock);
    }

    public class TestsForDemoAccountType {

        @Before
        public void setUp() {
            setLoginTypeOnData(loginTypeDemo);

            callCreate();
        }

        @Test
        public void userCredentialsAreCorrect() {
            assertUserCredentials();
        }

        @Test
        public void jnlpAdressIsForDemo() {
            assertJnlpAddress(jnlpDEMO);
        }

        @Test
        public void pinIsNotPresent() {
            assertFalse(credentials.maybePin().isPresent());
        }

        @Test
        public void noPinProviderInteraction() {
            verifyZeroInteractions(pinProviderMock);
        }
    }

    public class TestsForRealAccountType {

        @Before
        public void setUp() {
            setLoginTypeOnData(loginTypeReal);

            callCreate();
        }

        @Test
        public void userCredentialsAreCorrect() {
            assertUserCredentials();
        }

        @Test
        public void jnlpAdressIsForReal() {
            assertJnlpAddress(jnlpReal);
        }

        @Test
        public void pinIsPresent() {
            assertTrue(credentials.maybePin().isPresent());
        }

        @Test
        public void pinProviderWasCalled() {
            verify(pinProviderMock).getPin();
        }
    }
}
