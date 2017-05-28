package com.jforex.dzjforex.brokerstop.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerstop.SetSLParamsFactory;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SetSLParamsRunnerTest extends CommonUtilForTest {

    private SetSLParamsRunner setSLParamsRunner;

    @Mock
    private SetSLParamsFactory orderSetSLParamsMock;
    @Mock
    private SetSLParams setSLParamsMock;

    @Before
    public void setUp() {
        setSLParamsRunner = new SetSLParamsRunner(orderUtilMock, orderSetSLParamsMock);
    }

    private void makeOrderUtilPass() {
        when(orderUtilMock.paramsToObservable(setSLParamsMock))
            .thenReturn(Observable.just(orderEventA));
    }

    private void makeOrderUtilFail() {
        when(orderUtilMock.paramsToObservable(setSLParamsMock))
            .thenReturn(Observable.error(jfException));
    }

    private void makeSetSLParamsPass() {
        when(orderSetSLParamsMock.get(orderMockA, brokerStopData))
            .thenReturn(Single.just(setSLParamsMock));
    }

    private void makeSetSLParamsFail() {
        when(orderSetSLParamsMock.get(orderMockA, brokerStopData))
            .thenReturn(Single.error(jfException));
    }

    private TestObserver<Void> subscribe() {
        return setSLParamsRunner
            .get(orderMockA, brokerStopData)
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
