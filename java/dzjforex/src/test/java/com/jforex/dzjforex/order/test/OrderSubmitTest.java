package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderSubmit;
import com.jforex.dzjforex.test.util.CommonOrderForTest;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderSubmitTest extends CommonOrderForTest {

    private OrderSubmit orderSubmit;

    @Captor
    private ArgumentCaptor<SubmitParams> paramsCaptor;
    private OrderActionResult result;
    private final Instrument tradeInstrument = Instrument.EURUSD;
    private final OrderCommand command = OrderCommand.BUY;
    private final double amount = 0.12;
    private final String label = "TestLabel";
    private final double slPrice = 1.32456;

    @Before
    public void setUp() {
        orderSubmit = new OrderSubmit(tradeUtilMock);
    }

    private OrderActionResult callRun() {
        return orderSubmit.run(tradeInstrument,
                               command,
                               amount,
                               label,
                               slPrice);
    }

    @Test
    public void resultIsFAILWhenObservableFails() {
        setOrderUtilObservable(Observable.error(jfException));

        final OrderActionResult result = callRun();

        assertThat(result, equalTo(OrderActionResult.FAIL));
    }

    public class WhenSubmitCompletes {

        @Before
        public void setUp() {
            setOrderUtilObservable(Observable.empty());

            result = callRun();
        }

        @Test
        public void resultIsOK() {
            assertThat(result, equalTo(OrderActionResult.OK));
        }

        @Test
        public void submitParamsAreCorrect() throws Exception {
            verify(orderUtilMock).paramsToObservable(paramsCaptor.capture());
            final SubmitParams submitParams = paramsCaptor.getValue();

            assertRetryParams(submitParams.composeData());
        }

        @Test
        public void orderParamsAreCorrect() {
            verify(orderUtilMock).paramsToObservable(paramsCaptor.capture());
            final OrderParams orderParams = paramsCaptor
                .getValue()
                .orderParams();

            assertThat(orderParams.instrument(), equalTo(tradeInstrument));
            assertThat(orderParams.orderCommand(), equalTo(command));
            assertThat(orderParams.amount(), equalTo(amount));
            assertThat(orderParams.label(), equalTo(label));
            assertThat(orderParams.stopLossPrice(), equalTo(slPrice));
        }
    }
}
