package org.firefly.elevators_escalators.escalator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscalatorTransportTest {
 @Test void halfStepPreferred(){assertEquals(.5,EscalatorTransport.liftForClearance(true,true));}
 @Test void fullStepUsedForOneBlockRise(){assertEquals(1,EscalatorTransport.liftForClearance(false,true));}
 @Test void blockedHeadroomStopsTransport(){assertEquals(0,EscalatorTransport.liftForClearance(false,false));}
}
