package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import org.firefly.elevators_escalators.elevator.ShaftScanner;

import java.util.Objects;

/**
 * 一本用来诊断电梯多方块结构为何未成型的手册（使用原版书模型）
 */
public class HomeElevatorManualItem extends Item
{
    public HomeElevatorManualItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(@Nonnull UseOnContext ctx)
    {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = Objects.requireNonNull(ctx.getClickedPos());
        BlockState state = level.getBlockState(pos);
        Player player = ctx.getPlayer();

        if (state.getBlock() != EnrollBlocks.MAIN_CONTROLLER_BLOCK.get())
        {
            if (player != null)
            {
                player.displayClientMessage(text("请将宝典对准主控方块（顶层框架中的主控块）"), false);
            }
            return InteractionResult.SUCCESS;
        }

        // 尝试从被点的方块读取朝向（若是控制器），否则使用玩家朝向作为参考
    Direction facing = null;
    DirectionProperty facingProperty = Objects.requireNonNull(ControllerBlock.FACING);
        if (state.hasProperty(facingProperty))
        {
            facing = state.getValue(facingProperty);
        }
        else if (player != null)
        {
            facing = player.getDirection();
        }
        else
        {
            facing = Direction.NORTH;
        }

        ShaftScanner.DiagnosticReport report = ShaftScanner.diagnose(level, pos, facing);

        if (player != null)
        {
            player.displayClientMessage(text("===== 电梯结构诊断 ====="), false);
            if (report.hasProblems())
            {
                player.displayClientMessage(text("发现 " + report.problems().size() + " 个问题："), false);
                for (String problem : report.problems())
                {
                    player.displayClientMessage(text(" • " + problem), false);
                }
            }
            else
            {
                player.displayClientMessage(text("未发现结构问题。"), false);
            }

            if (!report.buildableFloors().isEmpty())
            {
                player.displayClientMessage(text("满足地板与净空条件的楼层数：" + report.buildableFloors().size()), false);
            }
            if (report.baseFloorY() != null)
            {
                player.displayClientMessage(text("当前基准楼层 Y = " + report.baseFloorY()), false);
            }

            if (!report.suggestions().isEmpty())
            {
                player.displayClientMessage(text("建议："), false);
                for (String suggestion : report.suggestions())
                {
                    player.displayClientMessage(text(" • " + suggestion), false);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static @Nonnull Component text(@Nonnull String message)
    {
        return Objects.requireNonNull(Component.literal(message));
    }
}
