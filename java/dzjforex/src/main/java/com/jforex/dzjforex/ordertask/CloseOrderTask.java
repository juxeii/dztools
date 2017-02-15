package com.jforex.dzjforex.ordertask;

import java.util.concurrent.Callable;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.PluginConfig;

public class CloseOrderTask implements Callable<IOrder> {

    private final IOrder order;
    private final double amount;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(CloseOrderTask.class);

    public CloseOrderTask(final IOrder order,
                          final double amount) {
        this.order = order;
        this.amount = amount;
    }

    @Override
    public IOrder call() {
        final boolean isPartialCloseRequest = amount < order.getAmount();
        try {
            if (!isPartialCloseRequest) {
                order.close();
                order.waitForUpdate(pluginConfig.ORDER_UPDATE_WAITTIME(), IOrder.State.CLOSED);
            } else
                order.close(amount);
        } catch (final JFException e) {
            logger.error("order close exc: " + e.getMessage());
        }
        return order;
    }
}
