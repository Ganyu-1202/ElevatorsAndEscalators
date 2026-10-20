package org.firefly.elevators_escalators.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class CabinCollisionMixin {
    // Both normal movement and vanilla step-up retries collect shapes here.
    // Injecting only collideBoundingBox leaves step-up's separate shape list unprotected.
    @ModifyVariable(method="collectColliders", at=@At("HEAD"), argsOnly=true)
    private static List<VoxelShape> elevators$appendCabin(List<VoxelShape> original, Entity entity, Level level, List<VoxelShape> ignored, AABB box) {
        if (entity==null || entity instanceof ElevatorPlatformEntity) return original;
        AABB sweep=box.inflate(1.0e-6);
        List<VoxelShape> result=null;
        for (var platform:level.getEntitiesOfClass(ElevatorPlatformEntity.class,sweep.inflate(0.5,3.25,0.5))) {
            if (!platform.canBeCollidedWith()) continue;
            for (VoxelShape shape:platform.cabinShapes()) {
                if (shape.bounds().intersects(sweep)) {
                    if (result==null) result=new ArrayList<>(original);
                    result.add(shape);
                }
            }
        }
        return result==null?original:result;
    }
}
