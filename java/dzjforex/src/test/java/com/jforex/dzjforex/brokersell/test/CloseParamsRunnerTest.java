package com.jforex.dzjforex.brokersell.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokersell.CloseParamsRunner;
import com.jforex.dzjforex.brokersell.CloseParamsFactory;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.CloseParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CloseParamsRunnerTest extends CommonUtilForTest {

    private CloseParamsRunner closeParamsRunner;

    @Mock
    private CloseParamsFactory orderCloseParamsMock;
    @Mock
    private CloseParams closeParamsMock;

    @Before
    public void setUp() {
        closeParamsRunner = new CloseParamsRunner(orderUtilMock, orderCloseParamsMock);
    }

    private void makeOrderUtilPass() {
        when(orderUtilMock.paramsToObservable(closeParamsMock))
            .thenReturn(Observable.just(orderEventA));
    }

    private void makeOrderUtilFail() {
        when(orderUtilMock.paramsToObservable(closeParamsMock))
            .thenReturn(Observable.error(jfException));
    }

    private void makeCloseParamsPass() {
        when(orderCloseParamsMock.get(orderMockA, brokerSellData))
            .thenReturn(Single.just(closeParamsMock));
    }

    private void makeCloseParamsFail() {
        when(orderCloseParamsMock.get(orderMockA, brokerSellData))
            .thenReturn(Single.error(jfException));
    }

    private TestObserver<Void> subscribe() {
        return closeParamsRunner
            .get(orderMockA, brokerSellData)
            .test();
    }

    @Test
    public void closeFailsWhenCloseParamsFail() {
        makeCloseParamsFail();

        subscribe().assertError(jfException);
    }

    public class OnCloseParamsPass {

        @Before
        public void setUp() {
            makeCloseParamsPass();
        }

        @Test
        public void closeFailsWhenOrderUtilFails() {
            makeOrderUtilFail();

            subscribe().assertError(jfException);
        }

        @Test
        public void closeSucceedsWhenOrderUtilSucceeds() {
            makeOrderUtilPass();

            subscribe().assertComplete();
        }
    }
}
