package com.jforex.dzjforex.order;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Maps;

import io.reactivex.Single;

public class OrderRepository {

    private final OrderLabelUtil orderLabelUtil;
    private final Map<Integer, IOrder> orderByTradeId = Maps.newHashMap();

    private final static Logger logger = LogManager.getLogger(OrderRepository.class);

    public OrderRepository(final OrderLabelUtil orderLabelUtil) {
        this.orderLabelUtil = orderLabelUtil;
    }

    public Single<IOrder> getByID(final int orderID) {
        return orderByTradeId.containsKey(orderID)
                ? Single.just(orderByTradeId.get(orderID))
                : Single.error(new JFException("OrderID " + orderID + " not in repository!"));
    }

    public void importZorroOrders(final List<IOrder> orders) {
        orders.forEach(this::storeOrder);
    }

    private void storeOrder(final IOrder order) {
        orderLabelUtil
            .idFromOrder(order)
            .onErrorComplete()
            .doOnSuccess(orderID -> logger.debug("Storing order with ID " + orderID + " to repository."))
            .subscribe(orderID -> orderByTradeId.put(orderID, order));
    }
}
