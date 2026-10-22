package org.firefly.elevators_escalators.elevator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiderSupportTest {
    @Test void longDescentKeepsFloorAndRoofRidersThroughArrival() {
        for (boolean roof : new boolean[]{false,true}) {
            for (double speed : new double[]{.025,.1,.4}) {
                double top=1000+(roof?4:.3076923077), feet=top;
                var sample=new RiderSupport.Sample(feet,top,0,roof);
                // Four ticks of interpolation/packet lag exceeds the old 1.25 limit.
                for(int tick=1;tick<=2400;tick++) {
                    top-=speed;
                    if(tick>4) feet-=speed;
                    assertTrue(RiderSupport.follows(sample,feet,top,0,tick));
                    sample=new RiderSupport.Sample(feet,top,tick,roof);
                }
                for(int tick=2401;tick<=2404;tick++) {
                    feet-=speed;
                    assertTrue(RiderSupport.follows(sample,feet,top,0,tick));
                    sample=new RiderSupport.Sample(feet,top,tick,roof);
                }
                assertEquals(top,feet,1e-8);
                assertTrue(RiderSupport.follows(sample,feet,top,0,2405));
            }
        }
    }
    @Test void ascentAccelerationKeepsTrustedSupportWithoutPullingPlayerDown() {
        double top=100,feet=100,speed=0;
        var sample=new RiderSupport.Sample(feet,top,0,false);
        for(int tick=1;tick<=80;tick++) {
            speed=Motion.approachSpeed(speed,100,.4);
            top+=speed;
            assertTrue(RiderSupport.follows(sample,feet,top,0,tick));
            double carry=DeckSupport.retainedCarry(feet,top-speed,top);
            assertTrue(carry>=0);
            feet+=carry;
            sample=new RiderSupport.Sample(feet,top,tick,false);
        }
    }
    @Test void rejectsNewFallerJumpStaleHistoryAndLargeSeparation() {
        var sample=new RiderSupport.Sample(100,100,10,false);
        assertFalse(RiderSupport.follows(null,100,100,0,11));
        assertFalse(RiderSupport.follows(sample,100.42,100,.42,11));
        assertFalse(RiderSupport.follows(sample,100,100,0,17));
        assertFalse(RiderSupport.follows(sample,100,100,0,9));
        assertFalse(RiderSupport.follows(sample,96,100,-1,11));
        assertFalse(RiderSupport.follows(sample,99,100,-1,11));
        assertFalse(RiderSupport.follows(sample,Double.NaN,100,0,11));
    }
    @Test void boundedLagCannotGrantUnlimitedFallImmunity() {
        var sample=new RiderSupport.Sample(102.7,100,10,false);
        assertFalse(RiderSupport.follows(sample,102.8,100,0,11));
    }
}
