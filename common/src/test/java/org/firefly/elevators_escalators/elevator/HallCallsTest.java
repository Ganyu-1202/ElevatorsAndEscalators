package org.firefly.elevators_escalators.elevator;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HallCallsTest {
 @Test void frameRequiresAllOrNone(){assertEquals(FrameBlueprint.Status.NONE,FrameBlueprint.classify(40,0,40));assertEquals(FrameBlueprint.Status.COMPLETE,FrameBlueprint.classify(0,40,40));assertEquals(FrameBlueprint.Status.PARTIAL,FrameBlueprint.classify(20,20,40));assertEquals(FrameBlueprint.Status.PARTIAL,FrameBlueprint.classify(0,39,40));}
 @Test void onlyFourContinuousColumnsAreRequired(){var p=FrameBlueprint.create(new AABB(-1,1,-1,2,13,2),new int[]{0,4,8},Direction.SOUTH);assertEquals(4*14,p.cells().size());for(var cell:p.cells())assertTrue((cell.getX()==-2||cell.getX()==2)&&(cell.getZ()==-2||cell.getZ()==2));}
 @Test void panelsAreOutsideCabinAndInFrontOfPosts(){for(var d:Direction.Plane.HORIZONTAL){var p=FrameBlueprint.create(new AABB(-1,1,-1,2,13,2),new int[]{0,4,8},d);assertEquals(3,p.panels().size());for(int i=0;i<3;i++){assertEquals(p.supports().get(i).relative(d),p.panels().get(i));assertTrue(p.cells().contains(p.supports().get(i)));}}}
 @Test void ascendingDoesNotInterceptDownCall(){var q=new StopQueue();q.add(20);q.addHall(8,-1);assertNull(q.intercept(0,20,.1));assertEquals(20,q.next(0,1));q.served(20,1);assertEquals(8,q.next(20,-1));}
 @Test void sameFloorOppositeCallsAreIndependent(){var q=new StopQueue();q.addHall(8,1);q.addHall(8,-1);q.served(8,1);assertTrue(q.contains(8));assertArrayEquals(new int[0],q.hallSnapshot(1));assertArrayEquals(new int[]{8},q.hallSnapshot(-1));q.served(8,-1);assertFalse(q.contains(8));}
 @Test void upCallCanBeInterceptedAndDuplicatesDoNotAccumulate(){var q=new StopQueue();assertTrue(q.addHall(8,1));assertFalse(q.addHall(8,1));assertEquals(8,q.intercept(0,20,.1));}
}
