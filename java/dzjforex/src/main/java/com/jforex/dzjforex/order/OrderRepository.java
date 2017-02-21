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

                final List<IOrder> historyOrders = historyProvider.ordersByInstrument(instrument,
                                                                                      from,
                                                                                      to);
                // final List<IOrder> filteredOrders = new ArrayList<>();
                // for (int i = 0; i < historyOrders.size(); ++i) {
                // final IOrder order = historyOrders.get(i);
                // if (labelUtil.isZorroOrder(order)) {
                // logger.debug("Found");
                // logger.debug("Found order " + order.getLabel() + " in
                // history");
                // filteredOrders.add(order);
                // }
                // }
                final List<IOrder> filteredOrders = historyOrders
                    .stream()
                    .filter(labelUtil::isZorroOrder)
                    .map(order -> {
                        logger.debug("Found order " + order.getLabel() + " inhistory");
                        return order;
                    })
                    .collect(Collectors.toList());

                logger.debug("Found " + filteredOrders.size() + " zorro history orders for " + instrument);
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
        logger.debug("Looking up orderID " + orderID + " in cache...");
        if (orderByTradeId.containsKey(orderID)) {
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
            order = engine.getOrder(labelUtil.labelFromId(orderID));
        } catch (final JFException e) {
            logger.error("Exception while engine.getOrder for " + orderID + "!");
        }
        if (order == null) {
            logger.error("Order with ID " + orderID + " not found in live engine. Seeking order ID in history now.");
            return orderFromHistory(orderID);
        }

        logger.debug("Found order ID " + orderID + " in live engine with label " + order.getLabel());
        storeOrder(orderID, order);
        return order;
    }

    private IOrder orderFromHistory(final int orderID) {
        logger.debug("Seeking orderID " + orderID + " in history...");
        if (historyOrderByTradeId == null)
            fillOrdersHistoryCache();

        logger.debug("After history import...");
        final IOrder order = historyOrderByTradeId.get(orderID);
        logger.debug("After map lookup.");
        if (order != null) {
            storeOrder(orderID, order);
            logger.debug("OrderID " + orderID + " found in history");
        } else {
            logger.warn("OrderID " + orderID + " not found in history!");
        }
        return order;
    }

    public void storeOrder(final int orderID,
                           final IOrder order) {
        orderByTradeId.put(orderID, order);
    }
}
