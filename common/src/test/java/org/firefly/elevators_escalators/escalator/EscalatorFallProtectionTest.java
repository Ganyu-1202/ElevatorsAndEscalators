package org.firefly.elevators_escalators.escalator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscalatorFallProtectionTest {
    @Test void normalDescentAndLandingAreEligible(){
        assertTrue(EscalatorFallProtection.safeMotion(-.15,.2f));
        assertTrue(EscalatorFallProtection.safeMotion(0,0));
    }
    @Test void actualFallsAndJumpsAreNotEligible(){
        assertFalse(EscalatorFallProtection.safeMotion(-.7,.5f));
        assertFalse(EscalatorFallProtection.safeMotion(-.1,5));
        assertFalse(EscalatorFallProtection.safeMotion(.42,0));
        assertFalse(EscalatorFallProtection.safeMotion(Double.NaN,0));
    }
}
