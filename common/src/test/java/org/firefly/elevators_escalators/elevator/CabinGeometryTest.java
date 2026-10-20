package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CabinGeometryTest {
    @Test void cabinInteriorIsNotSolid() { for(var b:CabinGeometry.shell(true)) assertFalse(b.contains(0,1.5,0)); }
    @Test void openDoorAllowsPassage() { assertFalse(CabinGeometry.shell(false).stream().anyMatch(b->b.contains(0,1.5,1.44))); }
    @Test void closedDoorBlocksPassage() { assertTrue(CabinGeometry.shell(true).stream().anyMatch(b->b.contains(0,1.5,1.44))); }
    @Test void roofAndWallsEncloseCabin() {
        assertTrue(CabinGeometry.shell(false).stream().anyMatch(b->b.contains(0,3.1,0)));
        assertTrue(CabinGeometry.shell(false).stream().anyMatch(b->b.contains(-1.44,1.5,0)));
        assertTrue(CabinGeometry.shell(false).stream().anyMatch(b->b.contains(0,1.5,-1.44)));
    }
    @Test void allPartsStayWithinShaftClearance() {
        for(var b:CabinGeometry.shell(true)) { assertTrue(b.x1()>=-1.5 && b.x2()<=1.5); assertTrue(b.z1()>=-1.5 && b.z2()<=1.5); assertTrue(b.y2()<=CabinGeometry.TOP); }
    }
}
