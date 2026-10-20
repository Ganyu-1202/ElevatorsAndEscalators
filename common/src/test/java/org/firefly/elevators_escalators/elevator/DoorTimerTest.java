package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DoorTimerTest {
    @Test void closesOnlyAfterOneHundredIdleTicks() { var t=new DoorTimer(); for(int i=0;i<99;i++) assertFalse(t.tick(true,false)); assertTrue(t.tick(true,false)); }
    @Test void obstructionResetsFullDelay() { var t=new DoorTimer(); for(int i=0;i<99;i++) t.tick(true,false); assertFalse(t.tick(true,true)); for(int i=0;i<99;i++) assertFalse(t.tick(true,false)); assertTrue(t.tick(true,false)); }
    @Test void activityResetsTimer() { var t=new DoorTimer(); for(int i=0;i<99;i++) t.tick(true,false); t.reset(); assertFalse(t.tick(true,false)); }
    @Test void closedDoorNeverTriggersTimeout() { var t=new DoorTimer(); for(int i=0;i<200;i++) assertFalse(t.tick(false,false)); }
}
