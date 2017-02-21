package com.jforex.dzjforex.order;

import java.time.Clock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.PluginConfig;

public class LabelUtil {

    private final Clock clock;
    private final PluginConfig pluginConfig;

    public LabelUtil(final Clock clock,
                     final PluginConfig pluginConfig) {
        this.clock = clock;
        this.pluginConfig = pluginConfig;
    }

    public String create() {
        return orderLabelPrefix() + nowAsInteger();
    }

    public int orderId(final IOrder order) {
        final String orderLabel = order.getLabel();
        final String idName = orderLabel.substring(orderLabelPrefix().length());
        return Integer.parseInt(idName);
    }

    public boolean isZorroOrder(final IOrder order) {
        final String label = order.getLabel();
        return label == null
                ? false
                : label.startsWith(pluginConfig.orderLabelPrefix());
    }

    public String labelFromId(final int orderId) {
        return pluginConfig.orderLabelPrefix() + String.valueOf(orderId);
    }

    private String orderLabelPrefix() {
        return pluginConfig.orderLabelPrefix();
    }

    private int nowAsInteger() {
        return dateMillisToInt(clock.millis());
    }

    private int dateMillisToInt(final long dateMillis) {
        return (int) (dateMillis % 1000000000);
    }
}
