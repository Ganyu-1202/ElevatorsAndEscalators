package org.firefly.elevators_escalators.escalator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscalatorFallProtectionTest {
    @Test void normalDescentAndLandingAreEligible(){
        assertTrue(EscalatorFallProtection.safeMotion(-.15,.2f));
        assertTrue(EscalatorFallProtection.safeMotion(0,0));
    }
    @Test void longSupportedDescentDoesNotAccumulateAfterVanillaSettlement(){
        float distance=0;
        for(int tick=0;tick<4000;tick++){
            double dy=tick%5==0?0:-.15;
            assertTrue(EscalatorFallProtection.safeMotion(dy,distance));
            distance=0; // HEAD, while feet are on a real ramp slice.
            distance-=(float)dy; // Vanilla may re-add the packet descent.
            assertTrue(EscalatorFallProtection.safeMotion(dy,distance));
            distance=0; // RETURN at the same supported position.
        }
        assertEquals(0,distance);
    }
    @Test void boundaryPacketStillAllowsPostSettlementReset(){
        assertTrue(EscalatorFallProtection.safeMotion(-.35,.35f));
        assertFalse(EscalatorFallProtection.safeMotion(-.3501,0));
        assertFalse(EscalatorFallProtection.safeMotion(0,1.01f));
    }
    @Test void actualFallsAndJumpsAreNotEligible(){
        assertFalse(EscalatorFallProtection.safeMotion(-.7,.5f));
        assertFalse(EscalatorFallProtection.safeMotion(-.1,5));
        assertFalse(EscalatorFallProtection.safeMotion(.42,0));
        assertFalse(EscalatorFallProtection.safeMotion(Double.NaN,0));
    }
}
