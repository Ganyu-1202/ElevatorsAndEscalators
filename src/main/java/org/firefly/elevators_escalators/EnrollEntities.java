package org.firefly.elevators_escalators;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;

import static org.firefly.elevators_escalators.ElevatorsEscalators.*;

import java.util.Objects;

public class EnrollEntities
{
        public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Objects.requireNonNull(Registries.ENTITY_TYPE), MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HomeElevatorCarriageEntity>> HOME_ELEVATOR_CARRIAGE =
            ENTITIES.register("home_elevator_carriage", () ->
                    EntityType.Builder.<HomeElevatorCarriageEntity>of(HomeElevatorCarriageEntity::new, MobCategory.MISC)
                            .sized(4.0F, 5.0F)
                            .build(Objects.requireNonNull(ResourceLocation.fromNamespaceAndPath(MODID, "home_elevator_carriage").toString()))
            );

}
