package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WallPanelBlock extends Block
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    // 厚度 1/16，高度 8/16，宽度 4/16（水平、竖直都居中）
    private static final double THICK  = 1.0 / 16.0;
    private static final double WIDTH  = 4.0 / 16.0;
    private static final double HEIGHT = 8.0 / 16.0;

    public WallPanelBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // 面板正面朝向玩家，因此 FACING = 玩家面朝方向的反方向
        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos)
    {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        // 支撑方块必须在朝向我们的一面是坚固的
        return levelReader.getBlockState(supportPos).isFaceSturdy(levelReader, supportPos, facing);
    }

    @Override
    public void neighborChanged(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block,
                                @Nonnull BlockPos fromPos, boolean isMoving)
    {
        if (!state.canSurvive(level, pos))
        {
            // 当支撑方块被移除时掉落并变为空气
            level.destroyBlock(pos, true);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public @Nonnull VoxelShape getShape(BlockState state,
                                        @Nonnull net.minecraft.world.level.BlockGetter world,
                                        @Nonnull BlockPos pos,
                                        @Nonnull CollisionContext context)
    {
        Direction f = state.getValue(FACING);

        // 水平居中
        double xMin = (1.0 - WIDTH) / 2.0;
        double xMax = xMin + WIDTH;
        // 竖直居中，对应 Y: 4/16 ~ 12/16
        double yMin = (1.0 - HEIGHT) / 2.0;
        double yMax = yMin + HEIGHT;

        return switch (f)
        {
            case NORTH ->
                // 正面朝北, 背面贴南侧墙 (Z = 1.0)
                    Shapes.box(xMin, yMin, 1.0 - THICK, xMax, yMax, 1.0);
            case SOUTH ->
                // 正面朝南, 背面贴北侧墙 (Z = 0.0)
                    Shapes.box(xMin, yMin, 0.0, xMax, yMax, THICK);
            case WEST  ->
                // 正面朝西, 背面贴东侧墙 (X = 1.0)
                    Shapes.box(1.0 - THICK, yMin, xMin, 1.0, yMax, xMax);
            case EAST  ->
                // 正面朝东, 背面贴西侧墙 (X = 0.0)
                    Shapes.box(0.0, yMin, xMin, THICK, yMax, xMax);
            default -> Shapes.block();
        };
    }

    @Override
    public @Nonnull BlockState rotate(BlockState state, @Nonnull LevelAccessor world, @Nonnull BlockPos pos,
                                      Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @Nonnull BlockState mirror(BlockState state, Mirror mirror)
    {
        Rotation rot = mirror.getRotation(state.getValue(FACING));
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }
}
