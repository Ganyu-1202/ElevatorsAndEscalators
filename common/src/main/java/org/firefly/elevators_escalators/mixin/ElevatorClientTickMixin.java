package org.firefly.elevators_escalators.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Carry after the player's interpolation origin is saved, before physics/sendPosition.
 * Ordinary entity insertion order is not a valid movement scheduling contract.
 */
@Mixin(ClientLevel.class)
public abstract class ElevatorClientTickMixin {
    @Unique private boolean elevators$updating;
    @Unique private boolean elevators$updated;

    @Inject(method="tickEntities",at=@At("HEAD"))
    private void elevators$begin(CallbackInfo ci){elevators$updated=false;}

    @Inject(method="tickNonPassenger",at=@At("HEAD"),cancellable=true)
    private void elevators$deferPlatform(Entity entity,CallbackInfo ci){
        if(entity instanceof ElevatorPlatformEntity&&!elevators$updating)ci.cancel();
    }

    @Inject(method="tickNonPassenger",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;tick()V"))
    private void elevators$beforePlayerPhysics(Entity entity,CallbackInfo ci){
        if(entity instanceof Player player&&player.isLocalPlayer())elevators$updatePlatforms();
    }

    @Inject(method="tickEntities",at=@At("TAIL"))
    private void elevators$withoutPlayer(CallbackInfo ci){elevators$updatePlatforms();}

    @Unique private void elevators$updatePlatforms(){
        if(elevators$updated)return;
        elevators$updated=true;
        ClientLevel level=(ClientLevel)(Object)this;
        elevators$updating=true;
        try{
            for(Entity entity:level.entitiesForRendering()){
                if(entity instanceof ElevatorPlatformEntity&&!entity.isRemoved()&&!entity.isPassenger())
                    level.tickNonPassenger(entity);
            }
        }finally{elevators$updating=false;}
    }
}
