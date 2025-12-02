package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import org.firefly.elevators_escalators.elevator.ElevatorShaftScanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class ControllerBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public ControllerBlock(Properties properties)
    {
        super(properties);
    this.registerDefaultState(Objects.requireNonNull(
        Objects.requireNonNull(this.stateDefinition.any())
            .setValue(Objects.requireNonNull(FACING), Direction.NORTH)
            .setValue(Objects.requireNonNull(ASSEMBLED), false)));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, ASSEMBLED);
    }

    @Override
    public @Nonnull BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
    {
    Direction direction = Objects.requireNonNull(context.getHorizontalDirection().getOpposite());
    return Objects.requireNonNull(
        Objects.requireNonNull(this.defaultBlockState())
            .setValue(Objects.requireNonNull(FACING), direction)
            .setValue(Objects.requireNonNull(ASSEMBLED), false));
    }

    @Override
    public @Nonnull BlockState rotate(@Nonnull BlockState state, @Nonnull Rotation rotation)
    {
        DirectionProperty facingProperty = Objects.requireNonNull(FACING);
    Direction rotated = Objects.requireNonNull(rotation).rotate(Objects.requireNonNull(state.getValue(facingProperty)));
    return Objects.requireNonNull(state.setValue(facingProperty, rotated));
    }

    @Override
    public @Nonnull BlockState mirror(@Nonnull BlockState state, @Nonnull Mirror mirror)
    {
        DirectionProperty facingProperty = Objects.requireNonNull(FACING);
    Rotation applied = Objects.requireNonNull(mirror.getRotation(Objects.requireNonNull(state.getValue(facingProperty))));
    return Objects.requireNonNull(rotate(state, applied));
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                            @Nullable LivingEntity placer, @Nonnull ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        if (!(placer instanceof Player player)) return;

    DirectionProperty facingProperty = Objects.requireNonNull(FACING);
    Direction facing = state.hasProperty(facingProperty) ? state.getValue(facingProperty) : player.getDirection();
        var report = ElevatorShaftScanner.diagnose(level, pos, facing);

        if (report.structureReady())
        {
            player.displayClientMessage(Objects.requireNonNull(Component.literal("结构检测通过，请使用轿厢方块点击主控完成装配。")), false);
        }
        else
        {
            player.displayClientMessage(Objects.requireNonNull(Component.literal("结构仍有 " + report.problems().size() + " 项问题，请使用电梯制造宝典查看详情。")), true);
            if (!report.problems().isEmpty())
            {
                player.displayClientMessage(Objects.requireNonNull(Component.literal("• " + report.problems().get(0))), true);
            }
        }
    }
}
