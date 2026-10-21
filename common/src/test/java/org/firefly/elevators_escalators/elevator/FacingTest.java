package org.firefly.elevators_escalators.elevator;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FacingTest {
    @Test void legacyOrdinalDefaultsToSouth() { assertEquals(Direction.SOUTH,CabinGeometry.facing(0)); assertEquals(Direction.SOUTH,CabinGeometry.facing(4)); }
    @Test void synchronizedFacingRoundTripsAllDirections() {
        for(var d:Direction.Plane.HORIZONTAL) assertEquals(d,CabinGeometry.facing(d.get2DDataValue()),d.name());
    }
    @Test void doorFollowsAllFourFaces() {
        for(var d:Direction.Plane.HORIZONTAL) {
            boolean atDoor=CabinGeometry.shell(true,d).stream().anyMatch(b->b.contains(d.getStepX()*1.44,.25+1.5,d.getStepZ()*1.44));
            assertTrue(atDoor,d.name());
            boolean atBack=CabinGeometry.shell(true,d).stream().anyMatch(b->b.contains(-d.getStepX()*1.44,1.0,-d.getStepZ()*1.44));
            assertTrue(atBack,d.name());
        }
    }
    @Test void interiorStaysOpenInEveryDirection() { for(var d:Direction.Plane.HORIZONTAL) for(var b:CabinGeometry.shell(true,d)) assertFalse(b.contains(0,1.5,0)); }
    @Test void shaftLandingOpensOnlyPlacerFacingSide() {
        for(var d:Direction.Plane.HORIZONTAL) {
            var cells=ShaftLayout.build(8,new int[]{0,4,8},d);
            int wallX=switch(d){case EAST->2;case WEST->-2;default->0;};
            int wallZ=switch(d){case NORTH->-2;case SOUTH->2;default->0;};
            assertFalse(cells.contains(new ShaftLayout.Cell(wallX,5,wallZ)),d.name());
            var opposite=cells.contains(new ShaftLayout.Cell(-wallX,5,-wallZ));
            assertTrue(opposite,d.name());
        }
    }
    @Test void doorFacesPlayerOppositeLookVector() {
        // Looking north means the player is south of the door.
        assertEquals(Direction.SOUTH,CabinGeometry.facingTowards(0,-1));
        assertEquals(Direction.NORTH,CabinGeometry.facingTowards(.01,1));
        assertEquals(Direction.WEST,CabinGeometry.facingTowards(1,0));
        assertEquals(Direction.EAST,CabinGeometry.facingTowards(-1,.2));
        assertEquals(Direction.WEST,CabinGeometry.facingTowards(1,-.2));
    }
    @Test void facingIsExactlyOppositeLookDirection() {
        for(var d:Direction.Plane.HORIZONTAL) assertEquals(d.getOpposite(),CabinGeometry.facingTowards(d.getStepX(),d.getStepZ()),d.name());
    }
}
