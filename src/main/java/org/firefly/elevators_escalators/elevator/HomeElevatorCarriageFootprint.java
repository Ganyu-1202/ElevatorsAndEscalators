package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.EnrollBlocks;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Shared helpers for placing or clearing the 2x2 carriage footprint inside the shaft.
 */
public final class HomeElevatorCarriageFootprint
{
    private HomeElevatorCarriageFootprint()
    {
    }

    public static void forEach(@Nonnull BlockPos innerOrigin,
                               int floorY,
                               @Nonnull Consumer<BlockPos> consumer)
    {
        Objects.requireNonNull(consumer);
        for (int dx = 0; dx < ElevatorShaftScanner.INNER_WIDTH; dx++)
        {
            for (int dz = 0; dz < ElevatorShaftScanner.INNER_DEPTH; dz++)
            {
                BlockPos pos = new BlockPos(innerOrigin.getX() + dx, floorY, innerOrigin.getZ() + dz);
                consumer.accept(pos);
            }
        }
    }

    public static void fillWithCarriage(@Nonnull Level level,
                                        @Nonnull BlockPos innerOrigin,
                                        int baseY)
    {
        BlockState state = Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CARRIAGE_BLOCK.get()).defaultBlockState();
        BlockState safeState = Objects.requireNonNull(state);
        forEach(innerOrigin, baseY, pos ->
        {
            level.setBlock(Objects.requireNonNull(pos), safeState, Block.UPDATE_ALL);
            level.setBlock(Objects.requireNonNull(pos.above()), safeState, Block.UPDATE_ALL);
        });
    }

    public static void clear(@Nonnull Level level,
                              @Nonnull BlockPos innerOrigin,
                              int baseY)
    {
        forEach(innerOrigin, baseY, pos ->
        {
            level.removeBlock(Objects.requireNonNull(pos), false);
            level.removeBlock(Objects.requireNonNull(pos.above()), false);
        });
    }
}
