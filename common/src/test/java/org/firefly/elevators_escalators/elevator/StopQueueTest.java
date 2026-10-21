package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StopQueueTest {
    @Test void requestsDeduplicate(){var q=new StopQueue();assertTrue(q.add(8));assertFalse(q.add(8));assertArrayEquals(new int[]{8},q.snapshot());}
    @Test void servesUpwardStopsBeforeReversing(){var q=new StopQueue();q.add(4);q.add(12);q.add(0);assertEquals(4,q.next(2,1));q.served(4);assertEquals(12,q.next(4,1));q.served(12);assertEquals(0,q.next(12,1));}
    @Test void servesDownwardStopsBeforeReversing(){var q=new StopQueue();q.add(4);q.add(12);q.add(20);assertEquals(12,q.next(16,-1));q.served(12);assertEquals(4,q.next(12,-1));q.served(4);assertEquals(20,q.next(4,-1));}
    @Test void stationaryChoosesNearest(){var q=new StopQueue();q.add(0);q.add(12);assertEquals(12,q.next(8,0));}
    @Test void interceptOnlyIfCanBrake(){var q=new StopQueue();q.add(4);q.add(12);assertNull(q.intercept(3.9,12,.4));assertEquals(4,q.intercept(0,12,.1));}
    @Test void emptyQueueHasNoTarget(){assertNull(new StopQueue().next(0,0));}
    @Test void selectingFloorsDoesNotResetDwell(){var timer=new DoorTimer();for(int i=0;i<70;i++)assertFalse(timer.tick(true,false));var q=new StopQueue();q.add(4);q.add(8);for(int i=0;i<29;i++)assertFalse(timer.tick(true,false));assertTrue(timer.tick(true,false));}
}
