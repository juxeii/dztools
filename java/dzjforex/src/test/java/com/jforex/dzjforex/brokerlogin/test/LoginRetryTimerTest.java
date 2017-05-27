package com.jforex.dzjforex.brokerlogin.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.dzjforex.brokerlogin.LoginRetryTimer;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class LoginRetryTimerTest extends CommonUtilForTest {

    private LoginRetryTimer loginRetryTimer;

    private final long loginRetryDelay = 1500L;

    @Before
    public void setUp() {
        when(pluginConfigMock.loginRetryDelay()).thenReturn(loginRetryDelay);

        loginRetryTimer = new LoginRetryTimer(pluginConfigMock);
    }

    @Test
    public void afterCreationLoginIsPermitted() {
        assertTrue(loginRetryTimer.isLoginPermitted());
    }

    public class WhenTimerStarted {

        @Before
        public void setUp() {
            loginRetryTimer.start();
        }

        @Test
        public void loginIsNotPermittedBeforeTimerHasExpired() {
            rxTestScheduler.advanceTimeInMillisBy(loginRetryDelay - 1);

            assertFalse(loginRetryTimer.isLoginPermitted());
        }

        @Test
        public void whenTimerExpirersLoginIsPermittedAgain() {
            rxTestScheduler.advanceTimeInMillisBy(loginRetryDelay);

            assertTrue(loginRetryTimer.isLoginPermitted());
        }
    }
}
