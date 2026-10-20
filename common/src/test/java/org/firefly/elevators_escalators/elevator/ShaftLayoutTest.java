package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ShaftLayoutTest {
    @Test void neverFillsCabinShaft() { for(var c:ShaftLayout.build(8,new int[]{0,4,8})) assertTrue(Math.abs(c.x())==2 || Math.abs(c.z())==2); }
    @Test void cornerColumnsAreContinuous() { var cells=ShaftLayout.build(8,new int[]{0,4,8}); for(int y=0;y<=12;y++) for(int x:new int[]{-2,2}) for(int z:new int[]{-2,2}) assertTrue(cells.contains(new ShaftLayout.Cell(x,y,z))); }
    @Test void southDoorwaysRemainOpen() { for(var c:ShaftLayout.build(8,new int[]{0,4,8})) if(c.z()==2 && Math.abs(c.x())<2) assertEquals(12,c.y()); }
    @Test void floorRingsAndRoofExist() { var cells=ShaftLayout.build(8,new int[]{0,4,8}); assertTrue(cells.contains(new ShaftLayout.Cell(0,5,-2))); assertTrue(cells.contains(new ShaftLayout.Cell(0,12,2))); }
    @Test void supportsMaximumTravelAndRejectsBadHeight() { assertFalse(ShaftLayout.build(32,FloorPlan.defaults(32)).isEmpty()); assertThrows(IllegalArgumentException.class,()->ShaftLayout.build(100,new int[0])); }
}
