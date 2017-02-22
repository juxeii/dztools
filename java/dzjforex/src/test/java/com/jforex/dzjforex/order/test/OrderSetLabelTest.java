package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import com.jforex.dzjforex.order.OrderSetLabel;
import com.jforex.dzjforex.order.OrderSetLabelResult;
import com.jforex.dzjforex.test.util.CommonOrderForTest;
import com.jforex.programming.order.task.params.basic.SetLabelParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderSetLabelTest extends CommonOrderForTest {

    private OrderSetLabel orderSetLabel;

    @Captor
    private ArgumentCaptor<SetLabelParams> paramsCaptor;
    private OrderSetLabelResult result;
    private final String oldLabel = "oldLabel";
    private final String newLabel = "newLabel";

    @Before
    public void setUp() {
        when(orderMock.getLabel()).thenReturn(oldLabel);

        orderSetLabel = new OrderSetLabel(tradeUtilMock);
    }

    @Test
    public void resultIsFAILWhenObservableFails() {
        setOrderUtilObservable(Observable.error(jfException));

        final OrderSetLabelResult result = orderSetLabel.run(orderMock, newLabel);

        assertThat(result, equalTo(OrderSetLabelResult.FAIL));
    }

    public class WhenSetLabelCompletes {

        @Before
        public void setUp() {
            setOrderUtilObservable(Observable.empty());

            result = orderSetLabel.run(orderMock, newLabel);
        }

        @Test
        public void resultIsOK() {
            assertThat(result, equalTo(OrderSetLabelResult.OK));
        }

        @Test
        public void setLabelParamsAreCorrect() throws Exception {
            verify(orderUtilMock).paramsToObservable(paramsCaptor.capture());
            final SetLabelParams setLabelParams = paramsCaptor.getValue();

            assertThat(setLabelParams.order(), equalTo(orderMock));
            assertThat(setLabelParams.newLabel(), equalTo(newLabel));
            assertRetryParams(setLabelParams.composeData());
        }
    }
}
