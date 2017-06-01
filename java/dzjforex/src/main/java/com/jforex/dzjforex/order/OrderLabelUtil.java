package com.jforex.dzjforex.order;

import java.time.Clock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Maybe;

public class OrderLabelUtil {

    private final Clock clock;
    private final String orderLabelPrefix;

    private static final int longModulo = 1000000000;

    public OrderLabelUtil(final Clock clock,
                          final PluginConfig pluginConfig) {
        this.clock = clock;

        orderLabelPrefix = pluginConfig.orderLabelPrefix();
    }

    public String create() {
        return orderLabelPrefix + nowAsInteger();
    }

    public Maybe<Integer> idFromOrder(final IOrder order) {
        return Maybe.defer(() -> idFromLabel(order.getLabel()));
    }

    public Maybe<Integer> idFromLabel(final String orderLabel) {
        return Maybe
            .fromCallable(() -> orderLabel)
            .filter(label -> label.startsWith(orderLabelPrefix))
            .map(label -> label.substring(orderLabelPrefix.length()))
            .map(idName -> Integer.parseInt(idName));
    }

    private int nowAsInteger() {
        return dateMillisToInt(clock.millis());
    }

    private int dateMillisToInt(final long dateMillis) {
        return (int) (dateMillis % longModulo);
    }
}
