package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ElevatorSpecTest {
    @Test void skipsZeroInNegativeToPositiveRange(){var s=new ElevatorSpec(3,3,4,-2,5,4);assertEquals(7,s.count());assertArrayEquals(new int[]{-2,-1,1,2,3,4,5},s.labels());assertArrayEquals(new int[]{0,4,8,12,16,20,24},s.offsets());}
    @Test void minimumAndMaximumCabinsAreValid(){assertNull(new ElevatorSpec(1,1,1,1,1,1).error(new ElevatorLimits()));assertNull(new ElevatorSpec(64,64,64,1,1,64).error(new ElevatorLimits()));}
    @Test void rejectsBadDimensionsAndFloorEndpoints(){for(var s:new ElevatorSpec[]{new ElevatorSpec(0,1,1,1,1,1),new ElevatorSpec(65,1,1,1,1,1),new ElevatorSpec(3,3,3,0,5,3),new ElevatorSpec(3,3,3,5,-2,3)})assertNotNull(s.error(new ElevatorLimits()));}
    @Test void countLimitIs256(){assertNull(new ElevatorSpec(3,3,3,-128,128,3).error(new ElevatorLimits()));assertNotNull(new ElevatorSpec(3,3,3,1,257,3).error(new ElevatorLimits()));}
    @Test void serverLimitsCanBeLowered(){var l=new ElevatorLimits();l.maxSize=8;l.maxFloors=10;assertNotNull(new ElevatorSpec(9,3,3,1,2,3).error(l));assertNotNull(new ElevatorSpec(3,3,3,1,11,3).error(l));}
    @Test void floorSpacingEqualsHeightForConfiguredSpecs(){var s=new ElevatorSpec(4,5,6,-1,3,6);assertEquals(18,s.travel());assertArrayEquals(new int[]{0,6,12,18},s.offsets());}
}
