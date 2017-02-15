package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.dzjforex.misc.PinProvider;
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

    private static final String jnlpDEMO = "jnlpDEMO";
    private static final String jnlpReal = "jnlpReal";
    private static final String username = "John";
    private static final String password = "Doe123";
    private static final String pin = "4242";
    private static final String loginTypeDemo = "Demo";
    private static final String loginTypeReal = "Real";

    @Before
    public void setUp() {
        when(pinProviderMock.getPin()).thenReturn(pin);
        when(pluginConfigMock.CONNECT_URL_DEMO()).thenReturn(jnlpDEMO);
        when(pluginConfigMock.CONNECT_URL_REAL()).thenReturn(jnlpReal);

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
