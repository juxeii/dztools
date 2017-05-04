package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerlogin.CredentialsFactory;
import com.jforex.dzjforex.brokerlogin.PinProvider;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.connection.LoginCredentials;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class CredentialsFactoryTest extends CommonUtilForTest {

    private CredentialsFactory credentialsFactory;

    @Mock
    private PinProvider pinProviderMock;
    @Mock
    private PluginConfig pluginConfigMock;
    private LoginCredentials credentials;

    @Before
    public void setUp() {
        when(pinProviderMock.getPin()).thenReturn(pin);
        when(pluginConfigMock.demoConnectURL()).thenReturn(jnlpDEMO);
        when(pluginConfigMock.realConnectURL()).thenReturn(jnlpReal);
        when(pluginConfigMock.demoLoginType()).thenReturn(loginTypeDemo);
        when(pluginConfigMock.realLoginType()).thenReturn(loginTypeReal);

        credentialsFactory = new CredentialsFactory(pinProviderMock, pluginConfigMock);
    }

    public class TestsForDemoAccountType {

        @Before
        public void setUp() {
            credentials = credentialsFactory.create(username,
                                                    password,
                                                    loginTypeDemo);
        }

        @Test
        public void userCredentialsAreCorrect() {
            assertUserCredentials();
        }

        @Test
        public void jnlpAdressIsForDemo() {
            assertThat(credentials.jnlpAddress(), equalTo(jnlpDEMO));
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
            credentials = credentialsFactory.create(username,
                                                    password,
                                                    loginTypeReal);
        }

        @Test
        public void userCredentialsAreCorrect() {
            assertUserCredentials();
        }

        @Test
        public void jnlpAdressIsForReal() {
            assertThat(credentials.jnlpAddress(), equalTo(jnlpReal));
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

    private void assertUserCredentials() {
        assertThat(credentials.username(), equalTo(username));
        assertThat(credentials.password(), equalTo(password));
    }
}
