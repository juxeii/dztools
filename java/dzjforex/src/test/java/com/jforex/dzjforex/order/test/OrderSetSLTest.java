package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderSetSL;
import com.jforex.dzjforex.test.util.CommonOrderForTest;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderSetSLTest extends CommonOrderForTest {

    private OrderSetSL orderSetSL;

    @Captor
    private ArgumentCaptor<SetSLParams> paramsCaptor;
    private OrderActionResult result;
    private final double newSL = 1.32456;

    @Before
    public void setUp() {
        orderSetSL = new OrderSetSL(tradeUtilMock);
    }

    @Test
    public void resultIsFAILWhenObservableFails() {
        setOrderUtilObservable(Observable.error(jfException));

        final OrderActionResult result = orderSetSL.run(orderMock, newSL);

        assertThat(result, equalTo(OrderActionResult.FAIL));
    }

    public class WhenSetSLCompletes {

        @Before
        public void setUp() {
            setOrderUtilObservable(Observable.empty());

            result = orderSetSL.run(orderMock, newSL);
        }

        @Test
        public void resultIsOK() {
            assertThat(result, equalTo(OrderActionResult.OK));
        }

        @Test
        public void setSLParamsAreCorrect() throws Exception {
            verify(orderUtilMock).paramsToObservable(paramsCaptor.capture());
            final SetSLParams setSLParams = paramsCaptor.getValue();

            assertThat(setSLParams.order(), equalTo(orderMock));
            assertThat(setSLParams.setSLTPMode(), equalTo(SetSLTPMode.PRICE));
            assertThat(setSLParams.priceOrPips(), equalTo(newSL));
            assertRetryParams(setSLParams.composeData());
        }
    }
}
