package org.firefly.elevators_escalators.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.firefly.elevators_escalators.escalator.EscalatorFallProtection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class EscalatorFallMixin {
    @Inject(method="doCheckFallDamage",at=@At("HEAD"))
    private void elevators$resetConveyorDescent(double dx,double dy,double dz,boolean onGround,CallbackInfo ci){
        ServerPlayer player=(ServerPlayer)(Object)this;
        EscalatorFallProtection.resetSupportedFall(player,dy);
        for(var platform:player.level().getEntitiesOfClass(
                org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity.class,
                player.getBoundingBox().expandTowards(0,-65.5,0).inflate(0,1.5,0))){
            if(platform.supportsForFallReset(player,dy)){player.fallDistance=0;break;}
        }
    }
}
