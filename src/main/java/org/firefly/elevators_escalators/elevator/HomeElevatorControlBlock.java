package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockInstance;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockSavedData;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * Floor-side control block that calls the elevator carriage to the current level.
 */
public class HomeElevatorControlBlock extends Block
{
    public HomeElevatorControlBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResult useWithoutItem(@Nonnull BlockState state,
                                                     @Nonnull Level level,
                                                     @Nonnull BlockPos pos,
                                                     @Nonnull Player player,
                                                     @Nonnull BlockHitResult hit)
    {
        Objects.requireNonNull(state);
        Objects.requireNonNull(level);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(player);
        Objects.requireNonNull(hit);

        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel))
        {
            return InteractionResult.PASS;
        }

        ElevatorMultiblockSavedData data = ElevatorMultiblockSavedData.get(serverLevel);
        ElevatorMultiblockInstance instance = data.findContaining(pos).orElse(null);
        if (instance == null || instance.floorYs().isEmpty())
        {
            return InteractionResult.PASS;
        }

        Integer logicalFloor = resolveLogicalFloor(instance, pos.getY());
        if (logicalFloor == null)
        {
            player.displayClientMessage(Objects.requireNonNull(Component.literal("未找到关联楼层，控制方块需要放在有效楼层的外圈上。")), true);
            return InteractionResult.PASS;
        }

        boolean moved = HomeElevatorCarriageRuntime.requestMoveToFloor(serverLevel, instance, logicalFloor);
        if (!moved)
        {
            player.displayClientMessage(Objects.requireNonNull(Component.literal("电梯暂时无法响应。")), true);
            return InteractionResult.PASS;
        }

        player.displayClientMessage(Objects.requireNonNull(Component.literal("电梯正在前往本楼层。")), false);
        return InteractionResult.CONSUME;
    }

    private static Integer resolveLogicalFloor(@Nonnull ElevatorMultiblockInstance instance, int y)
    {
        Objects.requireNonNull(instance);
        Integer logical = instance.yToLogicalFloor().get(y);
        if (logical != null)
        {
            return logical;
        }

        List<Integer> floors = instance.floorYs();
        int index = floors.indexOf(y);
        if (index >= 0)
        {
            return index;
        }

        int nearestIdx = -1;
        int nearestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < floors.size(); i++)
        {
            int diff = Math.abs(floors.get(i) - y);
            if (diff < nearestDistance)
            {
                nearestDistance = diff;
                nearestIdx = i;
            }
        }
        return nearestIdx >= 0 ? nearestIdx : null;
    }
}
