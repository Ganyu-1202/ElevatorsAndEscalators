package org.firefly.elevators_escalators;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;

import static org.firefly.elevators_escalators.ElevatorsEscalators.*;

public class EnrollEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HomeElevatorCarriageEntity>> HOME_ELEVATOR_CARRIAGE =
            ENTITIES.register("home_elevator_carriage", () ->
                    EntityType.Builder.<HomeElevatorCarriageEntity>of(HomeElevatorCarriageEntity::new, MobCategory.MISC)
                            .sized(4.0F, 5.0F)
                            .build(ResourceLocation.fromNamespaceAndPath(MODID, "home_elevator_carriage").toString())
            );

    public static final BlockPattern HOME_ELEVATOR_PATTERN = BlockPatternBuilder.start()
            .aisle("SSSS", "SSSS", "SSSS", "SSSS")
            .aisle("SSSS", "S  S", "S  S", "SLLS")
            .aisle("SSSS", "S  S", "S  S", "SUUS")
            .aisle("SSSS", "SSSS", "SSSS", "SSSS")
            .aisle("    ", "    ", " C  ", "    ")
            .where('S', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SMOOTH_STONE)))
            .where('L', BlockInWorld.hasState(bs ->
                    bs.getBlock() == EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()
                            && bs.hasProperty(net.minecraft.world.level.block.DoorBlock.HALF)
                            && bs.getValue(net.minecraft.world.level.block.DoorBlock.HALF)
                            == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER))
            .where('U', BlockInWorld.hasState(bs ->
                    bs.getBlock() == EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()
                            && bs.hasProperty(net.minecraft.world.level.block.DoorBlock.HALF)
                            && bs.getValue(net.minecraft.world.level.block.DoorBlock.HALF)
                            == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER))
            .where('C', BlockInWorld.hasState(BlockStatePredicate.forBlock(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get())))
            .where(' ', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
            .build();
}
