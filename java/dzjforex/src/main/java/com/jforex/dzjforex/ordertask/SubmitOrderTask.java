package com.jforex.dzjforex.ordertask;

import java.util.concurrent.Callable;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.config.PluginConfig;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

public class SubmitOrderTask implements Callable<IOrder> {

    private final IEngine engine;
    private final String orderLabel;
    private final Instrument instrument;
    private final OrderCommand cmd;
    private final double amount;
    private final double SLPrice;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(SubmitOrderTask.class);

    public SubmitOrderTask(final IEngine engine,
                           final String orderLabel,
                           final Instrument instrument,
                           final OrderCommand cmd,
                           final double amount,
                           final double SLPrice) {
        this.engine = engine;
        this.orderLabel = orderLabel;
        this.instrument = instrument;
        this.cmd = cmd;
        this.amount = amount;
        this.SLPrice = SLPrice;
    }

    @Override
    public IOrder call() {
        IOrder order = null;
        try {
            order = engine.submitOrder(orderLabel,
                                       instrument,
                                       cmd,
                                       amount,
                                       0f,
                                       pluginConfig.DEFAULT_SLIPPAGE(),
                                       SLPrice,
                                       0f,
                                       0L,
                                       "");
            order.waitForUpdate(pluginConfig.ORDER_UPDATE_WAITTIME(), IOrder.State.FILLED);
        } catch (final JFException e) {
            logger.error("submitOrder exception: " + e.getMessage());
        }
        return order;
    }
}
