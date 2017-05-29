package com.jforex.dzjforex.order;

import java.util.List;

import com.dukascopy.api.IOrder;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class OrderIDLookUp {

    private final Single<List<IOrder>> ordersProvider;
    private final OrderRepository orderRepository;

    public OrderIDLookUp(final Single<List<IOrder>> ordersProvider,
                         final OrderRepository orderRepository) {
        this.ordersProvider = ordersProvider;
        this.orderRepository = orderRepository;
    }

    public Maybe<IOrder> getByID(final int orderID) {
        return Single
            .defer(() -> ordersProvider)
            .flatMapCompletable(orderRepository::store)
            .andThen(Maybe.defer(() -> orderRepository.getByID(orderID)));
    }
}
