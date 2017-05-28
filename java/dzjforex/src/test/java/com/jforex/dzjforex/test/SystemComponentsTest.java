package com.jforex.dzjforex.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.SystemComponents;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class SystemComponentsTest extends CommonUtilForTest {

    private SystemComponents systemComponents;

    @Before
    public void setUp() {
        systemComponents = new SystemComponents(clientMock, pluginConfigMock);
    }

    @Test
    public void assertValidIClientInstance() {
        assertThat(systemComponents.client(), equalTo(clientMock));
    }

    @Test
    public void assertValidPluginConfig() {
        assertThat(systemComponents.pluginConfig(), equalTo(pluginConfigMock));
    }

    @Test
    public void assertValidClientUtil() {
        assertNotNull(systemComponents.clientUtil());
    }

    @Test
    public void assertValidInfoStrategy() {
        assertNotNull(systemComponents.infoStrategy());
    }
}
