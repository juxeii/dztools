package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerBuyData {

    private final String assetName;
    private final double slDistance;
    private final double tradeParams[];
    private final OrderCommand orderCommand;
    private final double amount;
    private final String orderLabel;
    private final int orderID;

    public BrokerBuyData(final String assetName,
                         final int contracts,
                         final double slDistance,
                         final double tradeParams[],
                         final TradeUtility tradeUtility) {
        this.assetName = assetName;
        this.slDistance = slDistance;
        this.tradeParams = tradeParams;

        amount = tradeUtility.contractsToAmount(contracts);
        orderCommand = tradeUtility.orderCommandForContracts(contracts);
        final OrderLabelUtil orderLabelUtil = tradeUtility.orderLabelUtil();
        orderLabel = orderLabelUtil.create();
        orderID = orderLabelUtil
            .idFromLabel(orderLabel)
            .blockingGet();
    }

    public String assetName() {
        return assetName;
    }

    public double amount() {
        return amount;
    }

    public double slDistance() {
        return slDistance;
    }

    public OrderCommand orderCommand() {
        return orderCommand;
    }

    public String orderLabel() {
        return orderLabel;
    }

    public int orderID() {
        return orderID;
    }

    public void fillOpenPrice(final IOrder order) {
        tradeParams[0] = order.getOpenPrice();
    }
}
