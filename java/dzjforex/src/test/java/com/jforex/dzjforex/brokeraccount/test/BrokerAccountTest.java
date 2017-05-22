package com.jforex.dzjforex.brokeraccount.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokeraccount.BrokerAccount;
import com.jforex.dzjforex.brokeraccount.BrokerAccountData;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerAccountTest extends CommonUtilForTest {

    private BrokerAccount brokerAccount;

    @Mock
    private AccountInfo accountInfoMock;
    @Mock
    private BrokerAccountData brokerAccountDataMock;
    private int handleResult;

    @Before
    public void setUp() {
        brokerAccount = new BrokerAccount(accountInfoMock);
    }

    private void setConnectedState(final boolean isConnected) {
        when(accountInfoMock.isConnected()).thenReturn(isConnected);
    }

    private void callHandle() {
        handleResult = brokerAccount.handle(brokerAccountDataMock);
    }

    @Test
    public void accountIsNotAvailableWhenNotConnected() {
        setConnectedState(false);

        callHandle();

        assertThat(handleResult, equalTo(ZorroReturnValues.ACCOUNT_UNAVAILABLE.getValue()));
    }

    public class OnAccountConnected {

        @Before
        public void setUp() {
            setConnectedState(true);
            callHandle();
        }

        @Test
        public void accountIsAvailable() {
            assertThat(handleResult, equalTo(ZorroReturnValues.ACCOUNT_AVAILABLE.getValue()));
        }

        @Test
        public void brokerAccountDataIsFilledWithAccountInfo() {
            verify(brokerAccountDataMock).fill(accountInfoMock);
        }
    }
}
