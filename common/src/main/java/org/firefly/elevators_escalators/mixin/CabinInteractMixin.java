package org.firefly.elevators_escalators.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.firefly.elevators_escalators.elevator.CabinInteraction;
import org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CabinInteractMixin {
    @Shadow public ServerPlayer player;

    @Redirect(method="handleInteract",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB elevators$interactionSurface(Entity entity){
        if(entity instanceof ElevatorPlatformEntity platform){
            var eye=player.getEyePosition();
            return CabinInteraction.surfaces(platform.cabinSpec(),platform.facing(),platform.doorsClosed(),platform.position())
                    .stream().min(java.util.Comparator.comparingDouble(box->box.distanceToSqr(eye)))
                    .orElseGet(entity::getBoundingBox);
        }
        return entity.getBoundingBox();
    }
}
