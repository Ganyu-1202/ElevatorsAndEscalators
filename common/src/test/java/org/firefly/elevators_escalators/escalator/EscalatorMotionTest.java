package org.firefly.elevators_escalators.escalator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EscalatorMotionTest {
    @Test void distantSegmentsDoNotNeedToSimulateUnloadedTicks(){
        var motion=new EscalatorMotion(100,0,0,.05);
        assertEquals(.05,motion.speed(10000),1e-9);
        assertEquals(motion.distanceAt(10000.5),motion.distanceAt(10000)+.025,1e-9);
    }
    @Test void changingDirectionPreservesDistanceAndCrossesZero(){
        var old=new EscalatorMotion(0,1,.05,.05);var reverse=old.retarget(100,-.05);
        assertEquals(old.distanceAt(100),reverse.distanceAt(100),1e-9);
        assertEquals(.05,reverse.speed(100),1e-9);
        assertEquals(0,reverse.speed(100+.05/.003),1e-9);
        assertEquals(-.05,reverse.speed(200),1e-9);
    }
    @Test void stoppedMotionHasNoPhaseDrift(){
        var motion=new EscalatorMotion(50,2,0,0);
        assertEquals(2,motion.distanceAt(500000),1e-9);
        assertEquals(0,motion.speed(500000),1e-9);
    }
    @Test void accelerationIntegratesAndThenCruises(){
        var motion=new EscalatorMotion(0,0,0,.06);
        assertEquals(.6,motion.distanceAt(20),1e-9);
        assertEquals(1.2,motion.distanceAt(30),1e-9);
    }
}
