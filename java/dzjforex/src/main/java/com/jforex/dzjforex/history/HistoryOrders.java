package com.jforex.dzjforex.history;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.order.OrderRepository;

import io.reactivex.Single;

public class HistoryOrders {

    private final HistoryOrdersProvider historyOrdersProvider;
    private final OrderRepository orderRepository;

    public HistoryOrders(final HistoryOrdersProvider historyOrdersProvider,
                         final OrderRepository orderRepository) {
        this.historyOrdersProvider = historyOrdersProvider;
        this.orderRepository = orderRepository;
    }

    public Single<IOrder> getByID(final int orderID) {
        return Single.defer(() -> historyOrdersProvider
            .get()
            .flatMapCompletable(orderRepository::store)
            .andThen(Single.defer(() -> orderRepository.getByID(orderID))));
    }
}
