package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.firefly.elevators_escalators.elevator.HomeElevatorAssembler;

import javax.annotation.Nonnull;

public class HomeElevatorCarriageBlockItem extends BlockItem
{
    public HomeElevatorCarriageBlockItem(@Nonnull Block block, @Nonnull Properties properties)
    {
        super(block, properties);
    }

    @Override
    public @Nonnull InteractionResult useOn(@Nonnull UseOnContext context)
    {
        Level level = context.getLevel();
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() != EnrollBlocks.MAIN_CONTROLLER_BLOCK.get())
        {
            sendHint(player, "请对准顶层主控制器使用轿厢方块。", true);
            return InteractionResult.FAIL;
        }

        Direction facing = resolveFacing(state, player);
        boolean assembled = HomeElevatorAssembler.tryAssemble(level, pos, facing, player);
        if (assembled)
        {
            if (player != null && !player.isCreative())
            {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    private static @Nonnull Direction resolveFacing(@Nonnull BlockState state, Player player)
    {
        DirectionProperty property = ControllerBlock.FACING;
        if (state.hasProperty(property))
        {
            return state.getValue(property);
        }
        if (player != null)
        {
            return player.getDirection();
        }
        return Direction.NORTH;
    }

    private static void sendHint(Player player, @Nonnull String message, boolean isError)
    {
        if (player != null)
        {
            player.displayClientMessage(Component.literal(message), isError);
        }
    }
}
