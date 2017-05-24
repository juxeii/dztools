package com.jforex.dzjforex.order;

import java.time.Clock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Maybe;

public class OrderLabelUtil {

    private final PluginConfig pluginConfig;
    private final Clock clock;

    private static final int longModulo = 1000000000;

    public OrderLabelUtil(final Clock clock,
                          final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        this.clock = clock;
    }

    public String create() {
        return orderLabelPrefix() + nowAsInteger();
    }

    public Maybe<Integer> idFromOrder(final IOrder order) {
        return Maybe.defer(() -> idFromLabel(order.getLabel()));
    }

    public Maybe<Integer> idFromLabel(final String label) {
        return Maybe.defer(() -> {
            final String orderLabelPrefix = orderLabelPrefix();
            if (label == null || !label.startsWith(orderLabelPrefix))
                return Maybe.empty();

            final String idName = label.substring(orderLabelPrefix.length());
            return Maybe.just(Integer.parseInt(idName));
        });
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
