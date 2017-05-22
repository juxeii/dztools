package com.jforex.dzjforex.brokerstop.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerstop.OrderSetSLParams;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SetSLParamsRunnerTest extends CommonUtilForTest {

    private SetSLParamsRunner setSLParamsRunner;

    @Mock
    private OrderSetSLParams orderSetSLParamsMock;
    @Mock
    private SetSLParams setSLParamsMock;

    @Before
    public void setUp() {
        setSLParamsRunner = new SetSLParamsRunner(orderUtilMock, orderSetSLParamsMock);
    }

    private void makeOrderUtilPass() {
        when(orderUtilMock.paramsToObservable(setSLParamsMock))
            .thenReturn(Observable.just(orderEvent));
    }

    private void makeOrderUtilFail() {
        when(orderUtilMock.paramsToObservable(setSLParamsMock))
            .thenReturn(Observable.error(jfException));
    }

    private void makeSetSLParamsPass() {
        when(orderSetSLParamsMock.get(orderMock, brokerStopData))
            .thenReturn(Single.just(setSLParamsMock));
    }

    private void makeSetSLParamsFail() {
        when(orderSetSLParamsMock.get(orderMock, brokerStopData))
            .thenReturn(Single.error(jfException));
    }

    private TestObserver<Void> subscribe() {
        return setSLParamsRunner
            .get(orderMock, brokerStopData)
            .test();
    }

    @Test
    public void setSLFailsWhenSetSLParamsFail() {
        makeSetSLParamsFail();

        subscribe().assertError(jfException);
    }

    public class OnSetSLParamsPass {

        @Before
        public void setUp() {
            makeSetSLParamsPass();
        }

        @Test
        public void setSLFailsWhenOrderUtilFails() {
            makeOrderUtilFail();

            subscribe().assertError(jfException);
        }

        @Test
        public void setSLSucceedsWhenOrderUtilSucceeds() {
            makeOrderUtilPass();

            subscribe().assertComplete();
        }
    }
}
