package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.JFException;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class InfoStrategyTest extends CommonUtilForTest {

    private InfoStrategy infoStrategy;

    @Before
    public void setUp() throws JFException {
        infoStrategy = new InfoStrategy();

        infoStrategy.onJFStart(contextMock);
        infoStrategy.onJFStop();
        infoStrategy.onJFAccount(null);
        infoStrategy.onJFMessage(null);
        infoStrategy.onJFTick(null, null);
        infoStrategy.onJFBar(null,
                             null,
                             null,
                             null);
    }

    @Test
    public void assertValidStrategyUtil() {
        assertNull(infoStrategy.strategyUtil());
    }

    @Test
    public void assertValidContext() {
        assertThat(infoStrategy.getContext(), equalTo(contextMock));
    }

    @Test
    public void assertValidAccount() {
        assertThat(infoStrategy.getAccount(), equalTo(accountMock));
    }

    @Test
    public void assertValidHistory() {
        assertThat(infoStrategy.getHistory(), equalTo(historyMock));
    }
}
