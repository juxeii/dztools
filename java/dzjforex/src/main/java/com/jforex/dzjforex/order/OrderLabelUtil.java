package com.jforex.dzjforex.order;

import java.time.Clock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.PluginConfig;

public class OrderLabelUtil {

    private final PluginConfig pluginConfig;
    private final Clock clock;

    private static final int longModulo = 1000000000;

    public OrderLabelUtil(final PluginConfig pluginConfig,
                          final Clock clock) {
        this.pluginConfig = pluginConfig;
        this.clock = clock;
    }

    public String create() {
        return orderLabelPrefix() + nowAsInteger();
    }

    public int idFromOrder(final IOrder order) {
        final String label = order.getLabel();
        return label == null
                ? 0
                : idFromLabel(order.getLabel());
    }

    public int idFromLabel(final String orderLabel) {
        final String idName = orderLabel.substring(orderLabelPrefix().length());
        return Integer.parseInt(idName);
    }

    public String labelFromId(final int orderId) {
        return pluginConfig.orderLabelPrefix() + String.valueOf(orderId);
    }

    public boolean hasZorroPrefix(final IOrder order) {
        final String label = order.getLabel();
        return label == null
                ? false
                : label.startsWith(pluginConfig.orderLabelPrefix());
    }

    private String orderLabelPrefix() {
        return pluginConfig.orderLabelPrefix();
    }

    private int nowAsInteger() {
        return dateMillisToInt(clock.millis());
    }

    private int dateMillisToInt(final long dateMillis) {
        return (int) (dateMillis % longModulo);
    }
}
