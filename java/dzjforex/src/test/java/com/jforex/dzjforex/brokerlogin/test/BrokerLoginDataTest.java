package com.jforex.dzjforex.brokerlogin.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerLoginDataTest extends CommonUtilForTest {

    private BrokerLoginData brokerLoginData;

    private final String accounts[] = new String[1];
    private final String accountID = "MyAccountID";

    @Before
    public void setUp() {
        brokerLoginData = new BrokerLoginData(username,
                                              password,
                                              loginTypeDemo,
                                              accounts);

        when(accountInfoMock.id()).thenReturn(accountID);

        brokerLoginData.fillAccounts(accountInfoMock);
    }

    @Test
    public void assertUsername() {
        assertThat(brokerLoginData.username(), equalTo(username));
    }

    @Test
    public void assertPassword() {
        assertThat(brokerLoginData.password(), equalTo(password));
    }

    @Test
    public void assertLoginType() {
        assertThat(brokerLoginData.loginType(), equalTo(loginTypeDemo));
    }

    @Test
    public void accountIDIsCorrectFilled() {
        assertThat(accounts[0], equalTo(accountID));
    }
}
