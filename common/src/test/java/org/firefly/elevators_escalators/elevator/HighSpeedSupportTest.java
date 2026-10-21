package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HighSpeedSupportTest {
    @Test void lostFractionOfTickIsRecoveredRatherThanAccumulating(){assertEquals(.6,DeckSupport.retainedCarry(100.8,101,101.4),1e-9);}
    @Test void fastRecentRiderIsRetainedButJumpingAndStaleAreNot(){assertTrue(DeckSupport.retained(100.5,101,0,.4,1));assertFalse(DeckSupport.retained(100.5,101,.42,.4,1));assertFalse(DeckSupport.retained(100.5,101,0,.4,7));assertFalse(DeckSupport.retained(98,101,0,.4,1));}
    @Test void repeatedFastAscentDoesNotAccumulateLag(){double top=100,feet=100;for(int i=0;i<600;i++){if(i%20==0)feet-=.2;assertTrue(DeckSupport.retained(feet,top,-.078,.4,1));feet+=DeckSupport.retainedCarry(feet,top,top+.4);top+=.4;assertEquals(top,feet,1e-8);}}
    @Test void recoveryDepthIsBoundedAndTracksSpeed(){assertEquals(.3,DeckSupport.recoveryDepth(0));assertEquals(1.25,DeckSupport.recoveryDepth(.4));assertEquals(1.25,DeckSupport.recoveryDepth(99));}
    @Test void neverPullsAscendingRiderDown(){assertEquals(0,DeckSupport.retainedCarry(101.5,101,101.4));}
}
