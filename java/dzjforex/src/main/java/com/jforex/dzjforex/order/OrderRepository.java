package com.jforex.dzjforex.order;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Maps;

import io.reactivex.Completable;
import io.reactivex.Observable;
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
                : Single.error(new JFException("No Zorro order with ID " + orderID + " in repository!"));
    }

    public Completable store(final List<IOrder> orders) {
        return Observable
            .fromIterable(orders)
            .flatMapCompletable(this::store, true);
    }

    public Completable store(final IOrder order) {
        return orderLabelUtil
            .idFromOrder(order)
            .doOnSuccess(orderID -> logger.debug("Storing Zorro order with ID " + orderID + " to repository."))
            .flatMapCompletable(orderID -> Completable.fromAction(() -> orderByTradeId.put(orderID, order)));
    }
}
