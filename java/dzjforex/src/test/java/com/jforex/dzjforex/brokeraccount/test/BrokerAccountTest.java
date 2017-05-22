package com.jforex.dzjforex.brokeraccount.test;

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
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerAccountTest extends CommonUtilForTest {

    private BrokerAccount brokerAccount;

    @Mock
    private AccountInfo accountInfoMock;
    @Mock
    private BrokerAccountData brokerAccountDataMock;

    @Before
    public void setUp() {
        brokerAccount = new BrokerAccount(accountInfoMock);
    }

    private void setConnectedState(final boolean isConnected) {
        when(accountInfoMock.isConnected()).thenReturn(isConnected);
    }

    private TestObserver<Integer> subscribe() {
        return brokerAccount
            .handle(brokerAccountDataMock)
            .test();
    }

    @Test
    public void accountIsNotAvailableWhenNotConnected() {
        setConnectedState(false);

        subscribe().assertValue(ZorroReturnValues.ACCOUNT_UNAVAILABLE.getValue());
    }

    public class OnAccountConnected {

        @Before
        public void setUp() {
            setConnectedState(true);
        }

        @Test
        public void accountIsAvailable() {
            subscribe().assertValue(ZorroReturnValues.ACCOUNT_AVAILABLE.getValue());
        }

        @Test
        public void brokerAccountDataIsFilledWithAccountInfo() {
            subscribe();

            verify(brokerAccountDataMock).fill(accountInfoMock);
        }
    }
}
