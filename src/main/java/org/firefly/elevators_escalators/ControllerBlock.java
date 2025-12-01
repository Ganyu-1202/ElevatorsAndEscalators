package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import org.firefly.elevators_escalators.elevator.ShaftScanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ControllerBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ControllerBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                            @Nullable LivingEntity placer, @Nonnull ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        if (!(placer instanceof Player player)) return;

        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : player.getDirection();
        var report = ShaftScanner.diagnose(level, pos, facing);

        if (report.structureReady())
        {
            player.displayClientMessage(Component.literal("结构检测通过，请使用轿厢方块点击主控完成装配。"), false);
        }
        else
        {
            player.displayClientMessage(Component.literal("结构仍有 " + report.problems().size() + " 项问题，请使用电梯制造宝典查看详情。"), true);
            if (!report.problems().isEmpty())
            {
                player.displayClientMessage(Component.literal("• " + report.problems().get(0)), true);
            }
        }
    }
}
