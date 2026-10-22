package org.firefly.elevators_escalators.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.*;
import org.firefly.elevators_escalators.elevator.CabinInteraction;
import org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class CabinPickMixin {
    @Inject(method="pick(F)V",at=@At("RETURN"))
    private void elevators$pickCabinSurfaces(float partial,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        var camera=mc.getCameraEntity();
        if(mc.level==null||mc.player==null||camera==null)return;
        Vec3 start=camera.getEyePosition(partial);
        double reach=mc.player.entityInteractionRange();
        Vec3 end=start.add(camera.getViewVector(partial).scale(reach));
        double nearest=reach*reach;
        if(mc.hitResult!=null&&mc.hitResult.getType()!=HitResult.Type.MISS)
            nearest=Math.min(nearest,start.distanceToSqr(mc.hitResult.getLocation()));
        // Platform physics bounds cover only the floor. Include tall cabins whose
        // floor is below the ray; leave their collision bounds entirely unchanged.
        var search=new AABB(start,end).inflate(1e-6).expandTowards(0,-64,0);
        for(var platform:mc.level.getEntitiesOfClass(ElevatorPlatformEntity.class,search)){
            if(!platform.isPickable())continue;
            Vec3 origin=platform.getPosition(partial);
            var hit=CabinInteraction.clip(CabinInteraction.surfaces(platform.cabinSpec(),platform.facing(),platform.doorsClosed(),origin),start,end);
            if(hit.isPresent()){
                double distance=start.distanceToSqr(hit.get());
                if(distance<nearest){
                    nearest=distance;
                    mc.hitResult=new EntityHitResult(platform,hit.get());
                    mc.crosshairPickEntity=platform;
                }
            }
        }
    }
}
