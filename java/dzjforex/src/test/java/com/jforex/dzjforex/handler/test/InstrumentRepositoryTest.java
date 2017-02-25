package com.jforex.dzjforex.handler.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.handler.InstrumentRepository;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class InstrumentRepositoryTest extends CommonUtilForTest {

    @Test
    public void invalidAssetNameRetrunsEmptyOptional() {
        assertFalse(InstrumentRepository
            .maybeFromName("Invalid")
            .isPresent());
    }

    public class AfterFirstValidAssetNameQuery {

        private final String assetName = "EUR/USD";
        private Instrument instrument;

        @Before
        public void setUp() {
            instrument = InstrumentRepository
                .maybeFromName(assetName)
                .get();
        }

        @Test
        public void instrumentIsCorrect() {
            assertThat(instrument, equalTo(Instrument.EURUSD));
        }
    }
}
