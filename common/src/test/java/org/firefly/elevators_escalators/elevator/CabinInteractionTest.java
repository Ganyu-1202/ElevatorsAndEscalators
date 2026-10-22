package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CabinInteractionTest {
    private Vec3 world(Vec3 local,ElevatorSpec spec,Direction facing){
        var b=CabinGeometry.rotate(spec.scale(new CabinGeometry.Box(local.x,local.y,local.z,local.x,local.y,local.z)),facing);
        return new Vec3(b.x1(),b.y1(),b.z1());
    }
    @Test void floorCeilingAndAllWallsArePickableAtEveryFacingAndSize(){
        for(var spec:new ElevatorSpec[]{ElevatorSpec.defaults(),new ElevatorSpec(5,7,12,1,3,12)})
            for(var facing:Direction.Plane.HORIZONTAL){
                var surfaces=CabinInteraction.surfaces(spec,facing,true,Vec3.ZERO);
                var start=world(new Vec3(0,1.5,0),spec,facing);
                for(var end:new Vec3[]{new Vec3(0,-1,0),new Vec3(0,4,0),new Vec3(-2,1.5,0),new Vec3(2,1.5,0),new Vec3(0,1.5,-2),new Vec3(0,1.5,2)})
                    assertTrue(CabinInteraction.clip(surfaces,start,world(end,spec,facing)).isPresent());
            }
    }
    @Test void openDoorAndEmptyInteriorDoNotInterceptRay(){
        for(var facing:Direction.Plane.HORIZONTAL){
            var spec=ElevatorSpec.defaults();
            var surfaces=CabinInteraction.surfaces(spec,facing,false,Vec3.ZERO);
            var start=world(new Vec3(0,1.5,0),spec,facing);
            assertTrue(CabinInteraction.clip(surfaces,start,world(new Vec3(0,1.5,2),spec,facing)).isEmpty());
            assertTrue(CabinInteraction.clip(surfaces,start,world(new Vec3(.5,1.5,.5),spec,facing)).isEmpty());
        }
    }
    @Test void usesNearestSurfaceAndRespectsRayLength(){
        var surfaces=CabinInteraction.surfaces(ElevatorSpec.defaults(),Direction.SOUTH,true,new Vec3(10,100,20));
        var eye=new Vec3(10,102,20);
        assertTrue(CabinInteraction.clip(surfaces,eye,new Vec3(10,102,19)).isEmpty());
        var hit=CabinInteraction.clip(surfaces,eye,new Vec3(10,102,17)).orElseThrow();
        assertEquals(18.625,hit.z,1e-8);
    }
    @Test void serverDistanceUsesRoofInsteadOfFarAwayFloor(){
        var spec=new ElevatorSpec(3,3,64,1,2,64);
        var eye=new Vec3(0,65.62,0);
        var surfaces=CabinInteraction.surfaces(spec,Direction.SOUTH,false,Vec3.ZERO);
        assertEquals(1.62*1.62,surfaces.stream().mapToDouble(b->b.distanceToSqr(eye)).min().orElseThrow(),1e-8);
    }
}
