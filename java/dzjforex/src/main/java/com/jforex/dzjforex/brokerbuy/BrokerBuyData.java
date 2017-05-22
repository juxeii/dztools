package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.PluginConfig;

public class BrokerBuyData {

    private final String instrumentName;
    private final double dStopDist;
    private final double tradeParams[];
    private final OrderCommand orderCommand;
    private final double amount;

    public BrokerBuyData(final String instrumentName,
                         final int contracts,
                         final double dStopDist,
                         final double tradeParams[],
                         final PluginConfig pluginConfig) {
        this.instrumentName = instrumentName;
        this.dStopDist = dStopDist;
        this.tradeParams = tradeParams;

        orderCommand = contracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
        amount = Math.abs(contracts) / pluginConfig.lotScale();
    }

    public String instrumentName() {
        return instrumentName;
    }

    public double amount() {
        return amount;
    }

    public double dStopDist() {
        return dStopDist;
    }

    public OrderCommand orderCommand() {
        return orderCommand;
    }

    public void fillOpenPrice(final IOrder order) {
        tradeParams[0] = order.getOpenPrice();
    }
}
