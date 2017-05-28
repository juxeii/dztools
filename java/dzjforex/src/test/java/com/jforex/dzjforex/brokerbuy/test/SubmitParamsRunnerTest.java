package com.jforex.dzjforex.brokerbuy.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.brokerbuy.SubmitParamsFactory;
import com.jforex.dzjforex.brokerbuy.SubmitParamsRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SubmitParamsRunnerTest extends CommonUtilForTest {

    private SubmitParamsRunner submitParamsRunner;

    @Mock
    private SubmitParamsFactory submitParamsFactoryMock;
    @Mock
    private SubmitParams submitParamsMock;

    @Before
    public void setUp() {
        submitParamsRunner = new SubmitParamsRunner(orderUtilMock, submitParamsFactoryMock);
    }

    private TestObserver<IOrder> subscribe() {
        return submitParamsRunner
            .get(instrumentForTest, brokerBuyData)
            .test();
    }

    private void setSubmitParamsFactoryResult(final Single<SubmitParams> result) {
        when(submitParamsFactoryMock.get(instrumentForTest, brokerBuyData))
            .thenReturn(result);
    }

    @Test
    public void submitFailsWhenSubmitParamsFail() {
        setSubmitParamsFactoryResult(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnSubmitParamsPass {

        @Before
        public void setUp() {
            setSubmitParamsFactoryResult(Single.just(submitParamsMock));
        }

        @Test
        public void submitFailsWhenOrderUtilFails() {
            when(orderUtilMock.paramsToObservable(submitParamsMock))
                .thenReturn(Observable.error(jfException));

            subscribe().assertError(jfException);
        }

        @Test
        public void submitSucceedsWhenOrderUtilSucceeds() {
            when(orderUtilMock.paramsToObservable(submitParamsMock))
                .thenReturn(Observable.just(orderEventA, orderEventB));

            subscribe()
                .assertValue(orderMockB)
                .assertComplete();
        }
    }
}
