package org.firefly.elevators_escalators.elevator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeckSupportTest {
    @Test void standingAndSmallNetworkDelayAreSupported() {
        assertTrue(DeckSupport.supported(101.25,101.25,0));
        assertTrue(DeckSupport.supported(101.15,101.25,-0.078));
    }
    @Test void creationQuarterBlockEmbeddingIsRecoverable() {
        assertTrue(DeckSupport.supported(101,101.25,0));
    }
    @Test void jumpingOrBelowDeckIsNotCaptured() {
        assertFalse(DeckSupport.supported(101.25,101.25,0.42));
        assertFalse(DeckSupport.supported(100.8,101.25,0));
        assertFalse(DeckSupport.supported(102,101.25,0));
    }
    @Test void movingDeckDoesNotDoubleApplyAnExistingUpwardCorrection() {
        assertEquals(0,DeckSupport.carryDelta(101.35,101.25,101.35),1e-9);
        assertEquals(0.1,DeckSupport.carryDelta(101.25,101.25,101.35),1e-9);
        assertEquals(-0.1,DeckSupport.carryDelta(101.25,101.25,101.15),1e-9);
    }
    @Test void ascendingDeckNeverPullsPredictedPlayerDown() {
        assertEquals(0,DeckSupport.carryDelta(101.36,101.25,101.26),1e-9);
        assertEquals(-0.1,DeckSupport.carryDelta(101.36,101.25,101.15),1e-9);
    }
    @Test void laggingServerPlayerCanBeRecordedWithoutCapturingJumpers() {
        assertTrue(DeckSupport.recordable(101,101.5,0));
        assertFalse(DeckSupport.recordable(101,102,0));
        assertFalse(DeckSupport.recordable(101.5,101.5,0.42));
    }
    @Test void rejectsNonFiniteState() {
        assertFalse(DeckSupport.supported(Double.NaN,1,0));
        assertFalse(DeckSupport.supported(1,1,Double.POSITIVE_INFINITY));
    }
}
