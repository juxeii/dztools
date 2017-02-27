package com.jforex.dzjforex.order;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;
import com.jforex.programming.order.task.params.position.MergePositionParams;

public class OrderMerge {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(OrderMerge.class);

    public OrderMerge(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public OrderMergeResult run(final Instrument mergeInstrument,
                                final String mergeOrderLabel) {
        final TaskParamsBase cancelSLTPComposeParams = TaskParamsBase.create()
            .doOnStart(() -> logger.info("Starting to cancel SL and TP "
                    + " for position " + mergeInstrument + "."))
            .doOnComplete(() -> logger.info("Cancel of SL and TP "
                    + " for position " + mergeInstrument + " done."))
            .doOnError(e -> logger.error("Cancel of SL and TP for position "
                    + mergeInstrument + " failed! Exception: " + e.getMessage()))
            .build();
        final TaskParamsBase batchCancelSLComposeParams = TaskParamsBase.create()
            .doOnStart(() -> logger.info("Start to cancel SL of position " + mergeInstrument))
            .doOnComplete(() -> logger.info("Cancelled SL of position " + mergeInstrument))
            .doOnError(e -> logger.info("Failed to cancel SL of position " +
                    mergeInstrument + "!Exception: " + e.getMessage()))
            .build();
        final TaskParamsBase batchCancelTPComposeParams = TaskParamsBase.create()
            .doOnStart(() -> logger.info("Start to cancel TP of position " + mergeInstrument))
            .doOnComplete(() -> logger.info("Cancelled TP of position " + mergeInstrument))
            .doOnError(e -> logger.info("Failed to cancel TP of position " +
                    mergeInstrument + "!Exception: " + e.getMessage()))
            .build();

        final Function<IOrder, CancelSLParams> cancelSLParamsFactory = order -> CancelSLParams
            .withOrder(order)
            .doOnStart(() -> logger.info("Start to cancel SL from order " + order.getLabel()
                    + " of position " + mergeInstrument + " current SL " + order.getStopLossPrice()))
            .doOnComplete(() -> logger.info("Cancelled SL from order " + order.getLabel()
                    + " of position " + mergeInstrument))
            .doOnError(e -> logger.info("Failed to cancel SL from order " + order.getLabel()
                    + " of position " + mergeInstrument + "!Exception: " + e.getMessage()))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        final Function<IOrder, CancelTPParams> cancelTPParamsFactory = order -> CancelTPParams
            .withOrder(order)
            .doOnStart(() -> logger.info("Start to cancel TP from order " + order.getLabel()
                    + " of position " + mergeInstrument + " current SL " + order.getTakeProfitPrice()))
            .doOnComplete(() -> logger.info("Cancelled TP from order " + order.getLabel()
                    + " of position " + mergeInstrument))
            .doOnError(e -> logger.info("Failed to cancel TP from order " + order.getLabel()
                    + " of position " + mergeInstrument + "!Exception: " + e.getMessage()))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        final MergeParamsForPosition mergeParamsForPosition = MergeParamsForPosition
            .newBuilder()
            .doOnStart(() -> logger.info("Trying to merge position " + mergeInstrument
                    + " with mergeOrderLabel " + mergeOrderLabel))
            .doOnError(err -> logger.error("Failed to merge position " + mergeInstrument
                    + "! " + err.getMessage()))
            .doOnComplete(() -> logger.info("Merging position " + mergeInstrument + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        final MergePositionParams mergePositionParams = MergePositionParams
            .newBuilder(mergeInstrument, mergeOrderLabel)
            .withMergeExecutionMode(CancelSLTPMode.MergeCancelSLAndTP)
            .withBatchCancelSLMode(BatchMode.MERGE)
            .withBatchCancelTPMode(BatchMode.MERGE)
            .withCancelSLTPParams(cancelSLTPComposeParams)
            .withBatchCancelSLParams(batchCancelSLComposeParams)
            .withBatchCancelTPParams(batchCancelTPComposeParams)
            .withCancelSLParamsFactory(cancelSLParamsFactory)
            .withCancelTPParamsFactory(cancelTPParamsFactory)
            .withMergeParamsForPosition(mergeParamsForPosition)
            .doOnStart(() -> logger.info("Starting to merge position " + mergeInstrument))
            .doOnComplete(() -> logger.info("Merging position " + mergeInstrument + " done."))
            .doOnError(e -> logger.error("Merging position " + mergeInstrument
                    + " failed! Exception: " + e.getMessage()))
            .build();

        return runOnOrderUtil(mergePositionParams);
    }

    private OrderMergeResult runOnOrderUtil(final MergePositionParams mergePositionParams) {
        final Throwable resultError = tradeUtil
            .orderUtil()
            .paramsToObservable(mergePositionParams)
            .ignoreElements()
            .blockingGet();

        return resultError == null
                ? OrderMergeResult.OK
                : OrderMergeResult.FAIL;
    }
}
