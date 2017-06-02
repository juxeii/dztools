package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.misc.Subscription;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class SubscriptionTest extends CommonUtilForTest {

    private Subscription subscription;

    @Captor
    private ArgumentCaptor<Set<Instrument>> instrumentsCaptor;
    private final List<Instrument> instrumentsToSubscribe = Lists.newArrayList();
    private Set<Instrument> subscribedInstruments;

    @Before
    public void setUp() {
        instrumentsToSubscribe.add(instrumentForTest);
        instrumentsToSubscribe.add(Instrument.EURBRL);
        instrumentsToSubscribe.add(Instrument.USDBRL);

        subscribedInstruments = Sets.newHashSet(instrumentsToSubscribe);

        subscription = new Subscription(clientMock);
    }

    private OngoingStubbing<Set<Instrument>> stubGetSubscribedInstruments() {
        return when(clientMock.getSubscribedInstruments());
    }

    @Test
    public void setCallIsDeferred() {
        subscription.set(instrumentsToSubscribe);

        verifyZeroInteractions(clientMock);
    }

    @Test
    public void setCallsWithCorrectInstruments() {
        subscription
            .set(instrumentsToSubscribe)
            .test()
            .assertComplete();

        verify(clientMock).setSubscribedInstruments(instrumentsCaptor.capture());
        assertThat(instrumentsCaptor.getValue(), containsInAnyOrder(instrumentForTest,
                                                                    Instrument.EURBRL,
                                                                    Instrument.USDBRL));
    }

    @Test
    public void instrumentsCallIsCorrect() {
        stubGetSubscribedInstruments().thenReturn(subscribedInstruments);

        assertThat(subscription.instruments(), equalTo(subscribedInstruments));
    }

    @Test
    public void whenInstrumentSubscribedIsCorrect() {
        stubGetSubscribedInstruments().thenReturn(subscribedInstruments);

        assertTrue(subscription.isSubscribed(instrumentForTest));
        assertTrue(subscription.isSubscribed(Instrument.EURBRL));
        assertTrue(subscription.isSubscribed(Instrument.USDBRL));
    }

    @Test
    public void whenInstrumentNotSubscribedIsCorrect() {
        stubGetSubscribedInstruments().thenReturn(Sets.newHashSet());

        assertFalse(subscription.isSubscribed(instrumentForTest));
        assertFalse(subscription.isSubscribed(Instrument.EURBRL));
        assertFalse(subscription.isSubscribed(Instrument.USDBRL));
    }
}
