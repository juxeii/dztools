package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.test.util.CommonOrderForTest;

public class OrderLabelUtilTest extends CommonOrderForTest {

    private OrderLabelUtil orderLabelUtil;

    private final int orderId = 123456789;
    private final String orderLabel = orderLabelPrefix + orderId;

    @Before
    public void setUp() {
        setOrderLabel(orderLabel);

        orderLabelUtil = new OrderLabelUtil(pluginConfigMock, clockMock);
    }

    private void setOrderLabel(final String label) {
        when(orderMock.getLabel()).thenReturn(label);
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
    public void idFromOrderExtractsId() {
        assertThat(orderLabelUtil.idFromOrder(orderMock), equalTo(orderId));
    }

    @Test
    public void idFromOrderWithNullLabelReturnsZero() {
        setOrderLabel(null);

        assertThat(orderLabelUtil.idFromOrder(orderMock), equalTo(0));
    }

    @Test
    public void idFromLabelExtractsId() {
        assertThat(orderLabelUtil.idFromLabel(orderLabel), equalTo(orderId));
    }

    @Test
    public void labelFromIdIsCorrect() {
        assertThat(orderLabelUtil.labelFromId(orderId), equalTo(orderLabelPrefix + orderId));
    }

    @Test
    public void hasZorroPrefixIsTrueForCorrectPrefix() {
        setOrderLabel("Zorro");

        assertTrue(orderLabelUtil.hasZorroPrefix(orderMock));
    }

    @Test
    public void hasZorroPrefixIsFalseForNotMatchingPrefix() {
        setOrderLabel("NoZorroLabel");

        assertFalse(orderLabelUtil.hasZorroPrefix(orderMock));
    }

    @Test
    public void hasZorroPrefixIsFalseForNullLabel() {
        setOrderLabel(null);

        assertFalse(orderLabelUtil.hasZorroPrefix(orderMock));
    }
}
