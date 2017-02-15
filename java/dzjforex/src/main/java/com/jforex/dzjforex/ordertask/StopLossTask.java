package com.jforex.dzjforex.ordertask;

import java.util.concurrent.Callable;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.settings.PluginConfig;

public class StopLossTask implements Callable<IOrder> {

    private final IOrder order;
    private final double SLPrice;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(StopLossTask.class);

    public StopLossTask(final IOrder order,
                        final double SLPrice) {
        this.order = order;
        this.SLPrice = SLPrice;
    }

    @Override
    public IOrder call() {
        try {
            order.setStopLossPrice(SLPrice);
            order.waitForUpdate(pluginConfig.ORDER_UPDATE_WAITTIME());
        } catch (final JFException e) {
            logger.error("Setting SL to " + SLPrice + " failed: " + e.getMessage());
        }
        return order;
    }
}
