package org.firefly.elevators_escalators.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ElevatorFallDamageMixin {
    @Inject(method="causeFallDamage",at=@At("HEAD"),cancellable=true)
    private void elevators$guardSupportedPassenger(float distance,float multiplier,DamageSource source,CallbackInfoReturnable<Boolean> ci){
        LivingEntity rider=(LivingEntity)(Object)this;
        if(rider.level().isClientSide)return;
        for(var platform:rider.level().getEntitiesOfClass(ElevatorPlatformEntity.class,
                rider.getBoundingBox().expandTowards(0,-65.5,0).inflate(0,1.5,0))){
            if(platform.supportsForFallReset(rider,0)){
                rider.fallDistance=0;
                ci.setReturnValue(false);
                return;
            }
        }
    }
}
