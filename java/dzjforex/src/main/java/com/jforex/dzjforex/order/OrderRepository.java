package com.jforex.dzjforex.order;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;

public class OrderRepository {

    private final OrderLabelUtil orderLabelUtil;
    private final Map<Integer, IOrder> orderByID = Maps.newHashMap();

    private final static Logger logger = LogManager.getLogger(OrderRepository.class);

    public OrderRepository(final OrderLabelUtil orderLabelUtil) {
        this.orderLabelUtil = orderLabelUtil;
    }

    public Maybe<IOrder> getByID(final int orderID) {
        return Maybe.defer(() -> orderByID.containsKey(orderID)
                ? Maybe.just(orderByID.get(orderID))
                : Maybe.empty());
    }

    public Completable store(final List<IOrder> orders) {
        return Observable
            .defer(() -> Observable.fromIterable(orders))
            .flatMapCompletable(this::store);
    }

    public Completable store(final IOrder order) {
        return Maybe
            .defer(() -> orderLabelUtil.idFromOrder(order))
            .flatMapCompletable(orderID -> storeToMap(orderID, order));
    }

    private Completable storeToMap(final int orderID,
                                   final IOrder order) {
        return Completable.fromAction(() -> {
            orderByID.put(orderID, order);
            logger.debug("Stored Zorro order with ID " + orderID + " to repository.");
        });
    }
}
