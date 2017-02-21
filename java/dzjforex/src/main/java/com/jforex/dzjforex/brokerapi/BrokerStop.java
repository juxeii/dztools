package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.order.SetSLHandler;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.math.MathUtil;

public class BrokerStop {

    private final SetSLHandler setSLHandler;
    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerStop.class);

    public BrokerStop(final SetSLHandler setSLHandler,
                      final TradeUtil tradeUtil) {
        this.setSLHandler = setSLHandler;
        this.tradeUtil = tradeUtil;
    }

    public int setSL(final int nTradeID,
                     final double dStop) {
        final IOrder order = tradeUtil.orderByID(nTradeID);
        if (order == null) {
            logger.error("Cannot set stop loss for trade with unknown ID " + nTradeID);
            return Constant.ADJUST_SL_FAIL;
        }
        if (!tradeUtil.isTradingAllowed())
            return Constant.ADJUST_SL_FAIL;

        logger.info("Trying to set stop loss for order ID " + nTradeID
                + " and dStop " + dStop);
        return setSLForValidOrderID(order, dStop);
    }

    private int setSLForValidOrderID(final IOrder order,
                                     final double dStop) {
        final double slPrice = MathUtil.roundPrice(dStop, order.getInstrument());
        if (tradeUtil.isSLPriceDistanceOK(order.getInstrument(), slPrice))
            setSLHandler.setSL(order, slPrice);

        return Constant.ADJUST_SL_OK;
    }
}
