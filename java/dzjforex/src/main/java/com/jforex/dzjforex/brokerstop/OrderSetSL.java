package com.jforex.dzjforex.brokerstop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.math.MathUtil;

import io.reactivex.Maybe;

public class OrderSetSL {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;
    private final StopLoss stopLoss;

    private final static Logger logger = LogManager.getLogger(OrderSetSL.class);

    public OrderSetSL(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;

        taskParams = tradeUtility.taskParams();
        stopLoss = tradeUtility.stopLoss();
    }

    public int run(final IOrder order,
                   final BrokerStopData brokerStopData) {
        final double dStop = brokerStopData.slPrice();

        return adaptedSLPrice(dStop, order.getInstrument())
            .map(slPrice -> taskParams.forSetSL(order, slPrice))
            .map(tradeUtility::runTaskParams)
            .map(this::evalSLResult)
            .defaultIfEmpty(ZorroReturnValues.ADJUST_SL_FAIL.getValue())
            .blockingGet();
    }

    private Maybe<Double> adaptedSLPrice(final double slPrice,
                                         final Instrument instrument) {
        final double roundedSLPrice = MathUtil.roundPrice(slPrice, instrument);
        if (!stopLoss.isPriceOK(instrument, slPrice)) {
            logger.error("Cannot set SL price to " + slPrice + " since the pip distance is too small!");
            return Maybe.empty();
        }
        return Maybe.just(roundedSLPrice);
    }

    private int evalSLResult(final OrderActionResult setSLResult) {
        return setSLResult == OrderActionResult.FAIL
                ? ZorroReturnValues.ADJUST_SL_FAIL.getValue()
                : ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
