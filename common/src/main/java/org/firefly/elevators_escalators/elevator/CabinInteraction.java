package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Picking geometry is separate from the deck's physics/entity bounding box. */
public final class CabinInteraction {
    private CabinInteraction() {}
    public static List<AABB> surfaces(ElevatorSpec spec,Direction facing,boolean closed,Vec3 origin){
        var boxes=new ArrayList<CabinGeometry.Box>();
        boxes.add(new CabinGeometry.Box(-1.5,0,-1.5,1.5,.25,1.5));
        boxes.addAll(CabinGeometry.shell(closed));
        return boxes.stream().map(spec::scale).map(b->CabinGeometry.rotate(b,facing))
                .map(b->new AABB(b.x1(),b.y1(),b.z1(),b.x2(),b.y2(),b.z2()).move(origin)).toList();
    }
    public static Optional<Vec3> clip(List<AABB> surfaces,Vec3 start,Vec3 end){
        Vec3 closest=null;
        double distance=Double.POSITIVE_INFINITY;
        for(var surface:surfaces){
            var hit=surface.contains(start)?Optional.of(start):surface.clip(start,end);
            if(hit.isPresent()&&start.distanceToSqr(hit.get())<distance){
                closest=hit.get();distance=start.distanceToSqr(closest);
            }
        }
        return Optional.ofNullable(closest);
    }
}
