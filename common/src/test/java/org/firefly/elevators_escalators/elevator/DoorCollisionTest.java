package org.firefly.elevators_escalators.elevator;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DoorCollisionTest {
    @Test void closedDoorStopsEntryAndExit() {
        var shapes=CabinGeometry.shell(true).stream().map(b->Shapes.create(new AABB(b.x1(),b.y1(),b.z1(),b.x2(),b.y2(),b.z2()))).toList();
        AABB inside=new AABB(-.3,.25,.7,.3,2.05,1.3);
        AABB outside=new AABB(-.3,.25,1.6,.3,2.05,2.2);
        assertEquals(.075,Shapes.collide(Direction.Axis.Z,inside,shapes,.4),1e-6);
        assertEquals(-.1,Shapes.collide(Direction.Axis.Z,outside,shapes,-.4),1e-6);
    }
    @Test void stepUpOntoDeckStillCannotPassClosedDoor() {
        var shapes=CabinGeometry.shell(true).stream().map(b->Shapes.create(new AABB(b.x1(),b.y1(),b.z1(),b.x2(),b.y2(),b.z2()))).toList();
        // Outside feet start level with deck base, then vanilla tries 0.25 and 0.6 block steps.
        AABB outside=new AABB(-.3,0,1.6,.3,1.8,2.2);
        for(double step:new double[]{.25,.6}) {
            assertEquals(-.1,Shapes.collide(Direction.Axis.Z,outside.move(0,step,0),shapes,-.4),1e-6);
        }
    }
    @Test void openDoorAllowsEntryAndExit() {
        var shapes=CabinGeometry.shell(false).stream().map(b->Shapes.create(new AABB(b.x1(),b.y1(),b.z1(),b.x2(),b.y2(),b.z2()))).toList();
        assertEquals(-.4,Shapes.collide(Direction.Axis.Z,new AABB(-.3,.25,1.6,.3,2.05,2.2),shapes,-.4),1e-6);
    }
}
