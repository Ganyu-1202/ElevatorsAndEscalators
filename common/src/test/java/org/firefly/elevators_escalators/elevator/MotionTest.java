package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MotionTest {
    @Test void movesUpAndDown() { assertEquals(1.1,Motion.step(1,9,.1),1e-9); assertEquals(8.9,Motion.step(9,1,.1),1e-9); }
    @Test void clampsWithoutOvershoot() { assertEquals(9,Motion.step(8.98,9,.1)); assertEquals(1,Motion.step(1.02,1,.1)); }
    @Test void staysAtStop() { assertEquals(3,Motion.step(3,3,.1)); }
    @Test void rejectsInvalidInput() { assertThrows(IllegalArgumentException.class,()->Motion.step(Double.NaN,1,.1)); assertThrows(IllegalArgumentException.class,()->Motion.step(0,1,0)); assertThrows(IllegalArgumentException.class,()->Motion.step(0,Double.POSITIVE_INFINITY,.1)); }
    @Test void reachesBothStops() { double y=1; for(int i=0;i<400;i++) y=Motion.step(y,33,.1); assertEquals(33,y); for(int i=0;i<400;i++) y=Motion.step(y,1,.1); assertEquals(1,y); }
    @Test void rampsFromRestWithoutSpeedJump() {
        double speed=0;
        for(int i=0;i<20;i++) { double next=Motion.approachSpeed(speed,8); assertTrue(Math.abs(next-speed)<=0.01000001); assertTrue(next<=Motion.SPEED+1e-9); speed=next; }
        assertEquals(Motion.SPEED,speed,1e-9);
        assertEquals(0,Motion.approachSpeed(speed,0));
        assertThrows(IllegalArgumentException.class,()->Motion.approachSpeed(Double.NaN,8));
    }
    @Test void travelBounds() { assertFalse(Motion.validHeight(3)); assertTrue(Motion.validHeight(4)); assertTrue(Motion.validHeight(32)); assertFalse(Motion.validHeight(33)); assertEquals(4,Motion.nextHeight(32)); assertEquals(12,Motion.nextHeight(8)); }
}
