//package com.jforex.dzjforex.brokerapi.test;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//
//import com.dukascopy.api.IOrder;
//import com.jforex.dzjforex.brokerapi.BrokerSell;
//import com.jforex.dzjforex.config.ZorroReturnValues;
//import com.jforex.dzjforex.order.OrderActionResult;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//
//@RunWith(HierarchicalContextRunner.class)
//public class BrokerSellTest extends CommonUtilForTest {
//
//    private BrokerSell brokerSell;
//
//    @Mock
//    private IOrder orderMock;
//    private final String newLabel = "newLabel";
//    private final int nTradeID = 1;
//    private final int nAmount = 1200;
//    private final double orderAmount = 0.00012;
//    private final double closeAmount = 0.00012;
//    private final double partialCloseAmount = 0.0001;
//
//    @Before
//    public void setUp() {
//        brokerSell = new BrokerSell(tradeUtilMock, orderCloseMock);
//    }
//
//    private int callCloseTrade() {
//        return brokerSell.closeTrade(nTradeID, nAmount);
//    }
//
//    @Test
//    public void whenTradingIsNotAllowedReturnValueIsBrokerSellFail() {
//        when(tradeUtilMock.isTradingAllowed()).thenReturn(false);
//
//        assertThat(callCloseTrade(),
//                   equalTo(ZorroReturnValues.BROKER_SELL_FAIL.getValue()));
//        verifyZeroInteractions(orderCloseMock);
//    }
//
//    public class WhenTradingIsAllowed {
//
//        @Before
//        public void setUp() {
//            when(tradeUtilMock.isTradingAllowed()).thenReturn(true);
//        }
//
//        @Test
//        public void whenOrderIdIsUnknownReturnValueIsBrokerSellFail() {
//            when(tradeUtilMock.orderByID(nTradeID)).thenReturn(null);
//
//            assertThat(callCloseTrade(),
//                       equalTo(ZorroReturnValues.BROKER_SELL_FAIL.getValue()));
//            verifyZeroInteractions(orderCloseMock);
//        }
//
//        public class WhenOrderIdIsKnown {
//
//            @Before
//            public void setUp() {
//                when(tradeUtilMock.contractsToAmount(nAmount)).thenReturn(closeAmount);
//                when(tradeUtilMock.orderByID(nTradeID)).thenReturn(orderMock);
//
//                when(orderLabelUtilMock.idFromOrder(orderMock)).thenReturn(nTradeID);
//                when(orderLabelUtilMock.create()).thenReturn(newLabel);
//
//                when(orderMock.getAmount()).thenReturn(orderAmount);
//            }
//
//            public class WhenOrderCloseFails {
//
//                @Before
//                public void setUp() {
//                    when(orderCloseMock.run(orderMock, closeAmount))
//                        .thenReturn(OrderActionResult.FAIL);
//                }
//
//                @Test
//                public void returnValueIsBrokerSellFail() {
//                    assertThat(callCloseTrade(),
//                               equalTo(ZorroReturnValues.BROKER_SELL_FAIL.getValue()));
//                }
//            }
//
//            public class WhenOrderCloseIsOK {
//
//                @Before
//                public void setUp() {
//                    when(orderCloseMock.run(orderMock, closeAmount))
//                        .thenReturn(OrderActionResult.OK);
//
//                    when(tradeUtilMock.orderByID(nTradeID))
//                        .thenReturn(orderMock);
//                }
//
//                public class OnPartialClose {
//
//                    @Before
//                    public void setUp() {
//                        when(tradeUtilMock.contractsToAmount(nAmount))
//                            .thenReturn(partialCloseAmount);
//                    }
//
//                    public class OnPartialCloseOK {
//
//                        private final int newTradeId = 2;
//
//                        @Before
//                        public void setUp() {
//                            when(orderLabelUtilMock.idFromOrder(orderMock))
//                                .thenReturn(newTradeId);
//                        }
//
//                        @Test
//                        public void returnValueIsNewTradeId() {
//                            callCloseTrade();
//
//                            assertThat(callCloseTrade(), equalTo(newTradeId));
//                        }
//                    }
//                }
//
//                public class OnFullClose {
//
//                    @Before
//                    public void setUp() {
//                        when(tradeUtilMock.contractsToAmount(nAmount))
//                            .thenReturn(closeAmount);
//                    }
//
//                    @Test
//                    public void returnValueIsCurrentTradeId() {
//                        assertThat(callCloseTrade(), equalTo(nTradeID));
//                    }
//                }
//            }
//        }
//    }
//}
