package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ElevatorSpeedTest {
    @Test void recommendationIncreasesWithFloorsAndCaps(){assertEquals(200,ElevatorSpeed.recommended(1));assertEquals(200,ElevatorSpeed.recommended(3));assertEquals(300,ElevatorSpeed.recommended(7));assertEquals(800,ElevatorSpeed.recommended(256));}
    @Test void sliderEndpointsAndUnits(){assertEquals(50,ElevatorSpeed.fromSlider(0));assertEquals(800,ElevatorSpeed.fromSlider(1));assertEquals(.1,ElevatorSpeed.perTick(200),1e-9);assertEquals(.4,ElevatorSpeed.perTick(800),1e-9);}
    @Test void serverRejectsOutOfRange(){assertNotNull(new ElevatorSpec(3,3,4,1,3,4,0).error(new ElevatorLimits()));assertNotNull(new ElevatorSpec(3,3,4,1,3,4,801).error(new ElevatorLimits()));assertNull(new ElevatorSpec(3,3,4,1,3,4,800).error(new ElevatorLimits()));}
    @Test void movementRespectsSelectedMaximum(){double v=0;for(int i=0;i<100;i++){v=Motion.approachSpeed(v,100,.25);assertTrue(v<=.25+1e-9);}assertEquals(.25,v,1e-9);assertEquals(0,Motion.approachSpeed(v,0,.25));}
    @Test void rejectsNonFiniteSpeed(){assertThrows(IllegalArgumentException.class,()->Motion.approachSpeed(0,8,Double.NaN));}
}
