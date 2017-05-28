package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class OrderLabelUtilTest extends CommonUtilForTest {

    private OrderLabelUtil orderLabelUtil;

    private final int orderId = 123456789;
    private final String orderLabel = orderLabelPrefix + orderId;

    @Before
    public void setUp() {
        setOrderLabel(orderLabel);

        orderLabelUtil = new OrderLabelUtil(clockMock, pluginConfigMock);
    }

    private void setOrderLabel(final String label) {
        when(orderMockA.getLabel()).thenReturn(label);
    }

    private void setClockMillis(final long millis) {
        when(clockMock.millis()).thenReturn(millis);
    }

    private int longToInt(final long millis) {
        return (int) (millis % 1000000000);
    }

    @Test
    public void createReturnsPrefixAndTime() {
        final long nowMillis = 1234567898L;
        setClockMillis(nowMillis);

        final String label = orderLabelUtil.create();

        assertThat(label, equalTo(orderLabelPrefix + longToInt(nowMillis)));
    }

    @Test
    public void twoCreateCallsGiveDifferentLabels() {
        final long nowMillisA = 1234567898L;
        setClockMillis(nowMillisA);
        final String labelA = orderLabelUtil.create();

        final long nowMillisB = 1234567723L;
        setClockMillis(nowMillisB);
        final String labelB = orderLabelUtil.create();

        assertFalse(labelA == labelB);
    }

    @Test
    public void idFromOrderCallIsDeffered() {
        orderLabelUtil.idFromOrder(orderMockA);

        verifyZeroInteractions(clockMock);
    }

    @Test
    public void idFromOrderExtractsID() {
        orderLabelUtil
            .idFromOrder(orderMockA)
            .test()
            .assertValue(orderId);
    }

    @Test
    public void idFromOrderCallWithNullLabelIsEmpty() {
        setOrderLabel(null);

        orderLabelUtil
            .idFromOrder(orderMockA)
            .test()
            .assertNoErrors()
            .assertNoValues();
    }

    @Test
    public void idFromOrderCallWithNoZorroPrefixIsEmpty() {
        when(orderMockA.getLabel()).thenReturn("NoZorroOrder");

        orderLabelUtil
            .idFromOrder(orderMockA)
            .test()
            .assertNoErrors()
            .assertNoValues();
    }

    @Test
    public void idFromLabelCallIsDeffered() {
        orderLabelUtil.idFromLabel(orderLabel);

        verifyZeroInteractions(clockMock);
    }

    @Test
    public void idFromLabelCallExtractsId() {
        orderLabelUtil
            .idFromLabel(orderLabel)
            .test()
            .assertValue(orderId);
    }

    @Test
    public void idFromLabelCallWithNoZorroPrefixIsEmpty() {
        orderLabelUtil
            .idFromLabel("NoZorroOrder")
            .test()
            .assertNoErrors()
            .assertNoValues();
    }
}
