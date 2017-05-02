package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.task.params.basic.SetSLParams;

public class BrokerStop {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerStop.class);

    public BrokerStop(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public int setSL(final BrokerStopData brokerStopData) {
        final Optional<IOrder> maybeOrder = tradeUtil.maybeOrderForTrading(brokerStopData.nTradeID());
        if (!maybeOrder.isPresent())
            return ZorroReturnValues.ADJUST_SL_FAIL.getValue();

        logger.info("Trying to set stop loss for order ID " + brokerStopData.nTradeID()
                + " and dStop " + brokerStopData.stopDistance());
        return setSLForValidOrderID(maybeOrder.get(), brokerStopData.stopDistance());
    }

    private int setSLForValidOrderID(final IOrder order,
                                     final double dStop) {
        final double slPrice = MathUtil.roundPrice(dStop, order.getInstrument());
        if (tradeUtil.isSLPriceDistanceOK(order.getInstrument(), slPrice)) {
            final SetSLParams setSLParams = tradeUtil
                .taskParams()
                .forSetSL(order, slPrice);
            tradeUtil.runTaskParams(setSLParams);
        }

        return ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
