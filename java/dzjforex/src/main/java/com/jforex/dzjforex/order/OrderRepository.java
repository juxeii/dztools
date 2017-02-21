package com.jforex.dzjforex.order;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.programming.misc.DateTimeUtil;

public class OrderRepository {

    private final IEngine engine;
    private final HistoryProvider historyProvider;
    private final BrokerSubscribe brokerSubscribe;
    private final Map<Integer, IOrder> orderByTradeId = new HashMap<>();
    private Map<Integer, IOrder> historyOrderByTradeId;
    private final PluginConfig pluginConfig;
    private final ServerTimeProvider serverTimeProvider;
    private final LabelUtil labelUtil;

    private final static Logger logger = LogManager.getLogger(OrderRepository.class);

    public OrderRepository(final IEngine engine,
                           final HistoryProvider historyProvider,
                           final BrokerSubscribe brokerSubscribe,
                           final PluginConfig pluginConfig,
                           final ServerTimeProvider serverTimeProvider,
                           final LabelUtil labelUtil) {
        this.engine = engine;
        this.historyProvider = historyProvider;
        this.brokerSubscribe = brokerSubscribe;
        this.pluginConfig = pluginConfig;
        this.serverTimeProvider = serverTimeProvider;
        this.labelUtil = labelUtil;
    }

    private void fillOrdersHistoryCache() {
        historyOrderByTradeId = new HashMap<>();

        brokerSubscribe
            .subscribedInstruments()
            .stream()
            .map(instrument -> {
                logger.debug("Fetching history orders for " + instrument);
                final LocalDateTime toDate = DateTimeUtil.dateTimeFromMillis(serverTimeProvider.get());
                final LocalDateTime fromDate = toDate.minusDays(pluginConfig.historyOrderInDays());
                final long to = DateTimeUtil.millisFromDateTime(toDate);
                final long from = DateTimeUtil.millisFromDateTime(fromDate);

                final List<IOrder> allOrders = historyProvider.ordersByInstrument(instrument,
                                                                                  from,
                                                                                  to);
                final List<IOrder> filteredOrders = allOrders
                    .stream()
                    .filter(labelUtil::isZorroOrder)
                    .collect(Collectors.toList());

                logger.debug("Found " + filteredOrders.size() + " zorro orders for " + instrument);
                return filteredOrders;
            })
            .flatMap(List::stream)
            .forEach(order -> {
                final int orderID = labelUtil.orderId(order);
                logger.debug("Storing history order with label " + order.getLabel()
                        + " and order ID " + orderID);
                historyOrderByTradeId.put(orderID, order);
            });
    }

    public IOrder orderByID(final int orderID) {
        logger.trace("Looking up orderID " + orderID + " in cache...");
        if (isOrderIDKnown(orderID)) {
            final IOrder order = orderByTradeId.get(orderID);
            logger.trace("Found orderID " + orderID
                    + " in cache with order label " + order.getLabel());
            return order;
        }
        return orderFromEngine(orderID);
    }

    private IOrder orderFromEngine(final int orderID) {
        logger.debug("Seeking orderID " + orderID + " in live engine...");
        IOrder order = null;
        try {
            order = engine.getOrder(String.valueOf(orderID));
        } catch (final JFException e) {
            logger.error("Order with ID " + orderID + " not found in live engine. Seeking order ID in history now.");
        }
        if (order == null) {
            return orderFromHistory(orderID);
        }

        logger.debug("Found order ID " + orderID + " in live engine with label " + order.getLabel());
        storeOrder(orderID, order);
        return order;
    }

    private IOrder orderFromHistory(final int orderID) {
        if (historyOrderByTradeId == null)
            fillOrdersHistoryCache();

        final IOrder order = historyOrderByTradeId.get(orderID);
        if (order != null)
            storeOrder(orderID, order);
        return order;
    }

    public void storeOrder(final int orderID,
                           final IOrder order) {
        orderByTradeId.put(orderID, order);
    }

    public boolean isOrderIDKnown(final int orderID) {
        return orderByTradeId.containsKey(orderID);
    }
}
