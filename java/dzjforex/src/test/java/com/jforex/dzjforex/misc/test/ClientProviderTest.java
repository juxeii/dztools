package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.misc.ClientProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClientFactory.class })
@PowerMockIgnore("javax.management.*")
public class ClientProviderTest extends CommonUtilForTest {

    @Before
    public void setUp() {
        mockStatic(ClientFactory.class);
    }

    private OngoingStubbing<IClient> stubGetDefaultInstance() throws Exception {
        return when(ClientFactory.getDefaultInstance());
    }

    @Test
    public void getReturnsCorrectInstance() throws Exception {
        stubGetDefaultInstance().thenReturn(clientMock);

        assertThat(ClientProvider.get(), equalTo(clientMock));
    }

    @Test(expected = RuntimeException.class)
    public void whenClientFactoryFailsTheExcpetionIsThrown() throws Exception {
        stubGetDefaultInstance().thenThrow(InstantiationException.class);

        ClientProvider.get();
    }
}
