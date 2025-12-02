package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Objects;

import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockInstance;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockSavedData;

public class HomeElevatorCarriageBlock extends Block
{
    private static final VoxelShape SHAPE = buildShape();

    @SuppressWarnings("null")
    private static VoxelShape buildShape()
    {
        VoxelShape floor = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
        VoxelShape ceiling = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        VoxelShape northWall = Block.box(0.0D, 2.0D, 0.0D, 16.0D, 14.0D, 2.0D);
        VoxelShape southWall = Block.box(0.0D, 2.0D, 14.0D, 16.0D, 14.0D, 16.0D);
        VoxelShape westWall = Block.box(0.0D, 2.0D, 0.0D, 2.0D, 14.0D, 16.0D);
        VoxelShape eastWall = Block.box(14.0D, 2.0D, 0.0D, 16.0D, 14.0D, 16.0D);
        return Objects.requireNonNull(Shapes.or(floor, ceiling, northWall, southWall, westWall, eastWall));
    }

    public HomeElevatorCarriageBlock(@Nonnull BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    public @Nonnull VoxelShape getShape(@Nonnull BlockState state,
                                        @Nonnull BlockGetter level,
                                        @Nonnull BlockPos pos,
                                        @Nonnull CollisionContext context)
    {
        return Objects.requireNonNull(SHAPE);
    }

    @Override
    public @Nonnull VoxelShape getCollisionShape(@Nonnull BlockState state,
                                                 @Nonnull BlockGetter level,
                                                 @Nonnull BlockPos pos,
                                                 @Nonnull CollisionContext context)
    {
        return Objects.requireNonNull(SHAPE);
    }

    @Override
    public boolean useShapeForLightOcclusion(@Nonnull BlockState state)
    {
        return true;
    }

    @Override
    public @Nonnull RenderShape getRenderShape(@Nonnull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult useWithoutItem(@Nonnull BlockState state,
                                            @Nonnull Level level,
                                            @Nonnull BlockPos pos,
                                            @Nonnull Player player,
                                            @Nonnull BlockHitResult hit)
    {
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !player.isShiftKeyDown())
        {
            return InteractionResult.PASS;
        }

        ElevatorMultiblockSavedData data = ElevatorMultiblockSavedData.get(serverLevel);
    ElevatorMultiblockInstance instance = data.findContaining(Objects.requireNonNull(pos)).orElse(null);
        if (instance == null || instance.floorYs().isEmpty())
        {
            return InteractionResult.PASS;
        }

        ElevatorMultiblockSavedData.CarriageRuntimeState runtimeState = data.getRuntimeState(instance.controllerPos())
                .orElse(ElevatorMultiblockSavedData.CarriageRuntimeState.blocks(instance.baseFloorY()));
        int currentIndex = instance.floorYs().indexOf(runtimeState.restingFloorY());
        if (currentIndex < 0)
        {
            currentIndex = 0;
        }
        int nextIndex = (currentIndex + 1) % instance.floorYs().size();
        boolean moved = HomeElevatorCarriageRuntime.requestMoveToFloor(serverLevel, instance, nextIndex);
        return moved ? InteractionResult.CONSUME : InteractionResult.PASS;
    }
}
