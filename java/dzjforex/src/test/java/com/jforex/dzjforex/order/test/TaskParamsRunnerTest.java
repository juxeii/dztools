//package com.jforex.dzjforex.order.test;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.stubbing.OngoingStubbing;
//
//import com.jforex.dzjforex.brokerbuy.OrderSubmitParams;
//import com.jforex.dzjforex.brokersell.OrderCloseParams;
//import com.jforex.dzjforex.brokerstop.OrderSetSLParams;
//import com.jforex.dzjforex.order.OrderActionResult;
//import com.jforex.dzjforex.order.TaskParamsRunner;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//import com.jforex.programming.order.task.params.TaskParams;
//import com.jforex.programming.order.task.params.basic.CloseParams;
//import com.jforex.programming.order.task.params.basic.SetSLParams;
//import com.jforex.programming.order.task.params.basic.SubmitParams;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//import io.reactivex.Observable;
//import io.reactivex.Single;
//
//@RunWith(HierarchicalContextRunner.class)
//public class TaskParamsRunnerTest extends CommonUtilForTest {
//
//    private TaskParamsRunner taskParamsRunner;
//
//    @Mock
//    private OrderSubmitParams orderSubmitParamsMock;
//    @Mock
//    private OrderCloseParams orderCloseParamsMock;
//    @Mock
//    private OrderSetSLParams orderSetSLParamsMock;
//    @Mock
//    private SubmitParams submitParamsMock;
//    @Mock
//    private CloseParams closeParamsMock;
//    @Mock
//    private SetSLParams setSLParamsMock;
//
//    @Before
//    public void setUp() {
//        taskParamsRunner = new TaskParamsRunner(orderUtilMock,
//                                                orderSubmitParamsMock,
//                                                orderCloseParamsMock,
//                                                orderSetSLParamsMock);
//    }
//
//    private void makeOrderUtilPass(final TaskParams taskParams) {
//        when(orderUtilMock.paramsToObservable(taskParams))
//            .thenReturn(Observable.just(orderEvent));
//    }
//
//    private void makeOrderUtilFail(final TaskParams taskParams) {
//        when(orderUtilMock.paramsToObservable(taskParams))
//            .thenReturn(Observable.error(jfException));
//    }
//
//    public class Submit {
//
//        private OngoingStubbing<Single<SubmitParams>> getStub() {
//            return when(orderSubmitParamsMock.get(instrumentForTest,
//                                                  brokerBuyData,
//                                                  orderLabel));
//        }
//
//        private void assertResult(final OrderActionResult result) {
//            assertThat(taskParamsRunner.startSubmit(instrumentForTest,
//                                                    brokerBuyData,
//                                                    orderLabel),
//                       equalTo(result));
//        }
//
//        @Test
//        public void submitFailsWhenSubmitParamsFails() {
//            getStub().thenReturn(Single.error(jfException));
//
//            assertResult(OrderActionResult.FAIL);
//        }
//
//        public class SubmitWithValidParams {
//
//            @Before
//            public void setUp() {
//                getStub().thenReturn(Single.just(submitParamsMock));
//            }
//
//            @Test
//            public void submitIsOKWhenOrderUtilIsOK() {
//                makeOrderUtilPass(submitParamsMock);
//
//                assertResult(OrderActionResult.OK);
//            }
//
//            @Test
//            public void submitFailsWhenOrderUtilFails() {
//                makeOrderUtilFail(submitParamsMock);
//
//                assertResult(OrderActionResult.FAIL);
//            }
//        }
//    }
//
//    public class Close {
//
//        private OngoingStubbing<Single<CloseParams>> getStub() {
//            return when(orderCloseParamsMock.get(orderMock, brokerSellData));
//        }
//
//        private void assertResult(final OrderActionResult result) {
//            assertThat(taskParamsRunner.startClose(orderMock, brokerSellData), equalTo(result));
//        }
//
//        @Test
//        public void closeFailsWhenCloseParamsFails() {
//            getStub().thenReturn(Single.error(jfException));
//
//            assertResult(OrderActionResult.FAIL);
//        }
//
//        public class CloseWithValidParams {
//
//            @Before
//            public void setUp() {
//                getStub().thenReturn(Single.just(closeParamsMock));
//            }
//
//            @Test
//            public void closeIsOKWhenOrderUtilIsOK() {
//                makeOrderUtilPass(closeParamsMock);
//
//                assertResult(OrderActionResult.OK);
//            }
//
//            @Test
//            public void closeFailsWhenOrderUtilFails() {
//                makeOrderUtilFail(closeParamsMock);
//
//                assertResult(OrderActionResult.FAIL);
//            }
//        }
//    }
//
//    public class SetSL {
//
//        private OngoingStubbing<Single<SetSLParams>> getStub() {
//            return when(orderSetSLParamsMock.get(orderMock, brokerStopData));
//        }
//
//        private void assertResult(final OrderActionResult result) {
//            assertThat(taskParamsRunner.startSetSL(orderMock, brokerStopData), equalTo(result));
//        }
//
//        @Test
//        public void setSLFailsWhenSetSLParamsFails() {
//            getStub().thenReturn(Single.error(jfException));
//
//            assertResult(OrderActionResult.FAIL);
//        }
//
//        public class SetSLWithValidParams {
//
//            @Before
//            public void setUp() {
//                getStub().thenReturn(Single.just(setSLParamsMock));
//            }
//
//            @Test
//            public void setSLIsOKWhenOrderUtilIsOK() {
//                makeOrderUtilPass(setSLParamsMock);
//
//                assertResult(OrderActionResult.OK);
//            }
//
//            @Test
//            public void setSLFailsWhenOrderUtilFails() {
//                makeOrderUtilFail(setSLParamsMock);
//
//                assertResult(OrderActionResult.FAIL);
//            }
//        }
//    }
//}
