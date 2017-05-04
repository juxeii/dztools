package com.jforex.dzjforex.brokerstop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.task.params.basic.SetSLParams;

public class BrokerStop {

    private final TradeUtility tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerStop.class);

    public BrokerStop(final TradeUtility tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public int setSL(final BrokerStopData brokerStopData) {
        return tradeUtil
            .maybeOrderForTrading(brokerStopData.nTradeID())
            .map(order -> setSLForValidOrderID(order, brokerStopData))
            .defaultIfEmpty(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }

    private int setSLForValidOrderID(final IOrder order,
                                     final BrokerStopData brokerStopData) {
        logger.info("Trying to set stop loss for order ID " + brokerStopData.nTradeID()
                + " and dStop " + brokerStopData.slPrice());
        final double slPrice = MathUtil.roundPrice(brokerStopData.slPrice(), order.getInstrument());
        if (tradeUtil.stopLoss().isSLPriceDistanceOK(order.getInstrument(), slPrice)) {
            final SetSLParams setSLParams = tradeUtil
                .taskParams()
                .forSetSL(order, slPrice);
            tradeUtil.runTaskParams(setSLParams);
        } else
            logger.error("Cannot set SL price to " + slPrice + " since the pip distance is too small!");

        return ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
