package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nonnull;

import org.firefly.elevators_escalators.EnrollBlocks;

import java.util.Objects;

public class HomeElevatorBlueprintItem extends Item
{
    private static final int FLOORS = 2;
    private static final int FLOOR_STEP = ElevatorShaftScanner.REQUIRED_CLEAR_HEIGHT + 1;
    private static final BlockState FLOOR_STATE = Blocks.SMOOTH_STONE.defaultBlockState();

    public HomeElevatorBlueprintItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResult useOn(@Nonnull UseOnContext ctx)
    {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (ctx.getClickedFace() != Direction.UP)
        {
            notifyPlayer(ctx.getPlayer(), "请在要建造的位置点击地面顶部。", true);
            return InteractionResult.FAIL;
        }

        BlockPos innerOrigin = Objects.requireNonNull(ctx.getClickedPos()).above();
        Direction doorDir = Objects.requireNonNull(resolveDoorDirection(ctx));

        if (!hasEnoughSpace(level, innerOrigin))
        {
            notifyPlayer(ctx.getPlayer(), "空间不足：请腾出 4x4x" + (FLOOR_STEP * FLOORS + ElevatorShaftScanner.REQUIRED_CLEAR_HEIGHT) + " 的区域。", true);
            return InteractionResult.FAIL;
        }

        buildStructure(level, innerOrigin, doorDir);
        notifyPlayer(ctx.getPlayer(), "已生成 2 层家用电梯结构，记得在最上层框架安装主控方块并激活。", false);
        return InteractionResult.SUCCESS;
    }

    private static void notifyPlayer(Player player, @Nonnull String message, boolean isError)
    {
        if (player != null)
        {
            player.displayClientMessage(text(message), isError);
        }
    }

    private static @Nonnull Direction resolveDoorDirection(@Nonnull UseOnContext ctx)
    {
        Player player = ctx.getPlayer();
        Direction dir = player != null ? player.getDirection() : ctx.getHorizontalDirection();
        if (!dir.getAxis().isHorizontal())
        {
            dir = Direction.SOUTH;
        }
        return dir;
    }

    private static boolean hasEnoughSpace(Level level, BlockPos innerOrigin)
    {
        int baseY = innerOrigin.getY();
        int topFloorY = baseY + (FLOORS - 1) * FLOOR_STEP;
        int topY = topFloorY + ElevatorShaftScanner.REQUIRED_CLEAR_HEIGHT + 1;

        int outerMinX = innerOrigin.getX() - ElevatorShaftScanner.FRAME_MARGIN;
        int outerMaxX = innerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;
        int outerMinZ = innerOrigin.getZ() - ElevatorShaftScanner.FRAME_MARGIN;
        int outerMaxZ = innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;

        for (int y = baseY; y <= topY; y++)
        {
            for (int x = outerMinX; x <= outerMaxX; x++)
            {
                for (int z = outerMinZ; z <= outerMaxZ; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && !state.canBeReplaced())
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void buildStructure(Level level, BlockPos innerOrigin, Direction doorDir)
    {
        int baseY = innerOrigin.getY();
        for (int floorIndex = 0; floorIndex < FLOORS; floorIndex++)
        {
            int floorY = baseY + floorIndex * FLOOR_STEP;
            buildFloor(level, innerOrigin, floorY, doorDir, floorIndex == 0);
        }
    }

    private static void buildFloor(Level level, BlockPos innerOrigin, int y, Direction doorDir, boolean isBaseFloor)
    {
        int clearanceTop = y + ElevatorShaftScanner.REQUIRED_CLEAR_HEIGHT;
        if (isBaseFloor)
        {
            placeFloor(level, innerOrigin, y);
            clearInterior(level, innerOrigin, y + 1, clearanceTop);
        }
        else
        {
            clearInterior(level, innerOrigin, y, clearanceTop);
        }
        placeRing(level, innerOrigin, y, isBaseFloor, doorDir);
        placeControlBlock(level, innerOrigin, y, Objects.requireNonNull(doorDir));
        placeDoor(level, innerOrigin, y, Objects.requireNonNull(doorDir));
    }

    private static void placeFloor(Level level, BlockPos innerOrigin, int y)
    {
        for (int dx = 0; dx < ElevatorShaftScanner.INNER_WIDTH; dx++)
        {
            for (int dz = 0; dz < ElevatorShaftScanner.INNER_DEPTH; dz++)
            {
                BlockPos pos = new BlockPos(innerOrigin.getX() + dx, y, innerOrigin.getZ() + dz);
                level.setBlock(pos, Objects.requireNonNull(FLOOR_STATE), Block.UPDATE_ALL);
            }
        }
    }

    private static void clearInterior(Level level, BlockPos innerOrigin, int fromY, int toY)
    {
        for (int y = fromY; y <= toY; y++)
        {
            for (int dx = 0; dx < ElevatorShaftScanner.INNER_WIDTH; dx++)
            {
                for (int dz = 0; dz < ElevatorShaftScanner.INNER_DEPTH; dz++)
                {
                    BlockPos pos = new BlockPos(innerOrigin.getX() + dx, y, innerOrigin.getZ() + dz);
                    level.setBlock(pos, Objects.requireNonNull(Blocks.AIR.defaultBlockState()), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void placeRing(Level level, BlockPos innerOrigin, int y, boolean isBaseFloor, Direction doorDir)
    {
        BlockPos doorPos = computeDoorBase(innerOrigin, y, doorDir);

        int outerMinX = innerOrigin.getX() - ElevatorShaftScanner.FRAME_MARGIN;
        int outerMaxX = innerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;
        int outerMinZ = innerOrigin.getZ() - ElevatorShaftScanner.FRAME_MARGIN;
        int outerMaxZ = innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;

        for (int x = outerMinX; x <= outerMaxX; x++)
        {
            for (int z = outerMinZ; z <= outerMaxZ; z++)
            {
                boolean edge = (x == outerMinX || x == outerMaxX || z == outerMinZ || z == outerMaxZ);
                if (!edge) continue;
                BlockPos pos = new BlockPos(x, y, z);
                if (pos.equals(doorPos))
                {
                    continue;
                }

                BlockState state = isBaseFloor ? positionFrameState() : frameState();
                level.setBlock(pos, Objects.requireNonNull(state), Block.UPDATE_ALL);
            }
        }
    }

    private static void placeControlBlock(Level level, BlockPos innerOrigin, int y, Direction doorDir)
    {
        BlockPos controlPos = findControlPlacement(innerOrigin, y, doorDir);
        if (controlPos != null)
        {
            level.setBlock(controlPos, Objects.requireNonNull(controlBlockState()), Block.UPDATE_ALL);
        }
    }

    private static BlockPos findControlPlacement(BlockPos innerOrigin, int y, Direction doorDir)
    {
        BlockPos doorPos = computeDoorBase(innerOrigin, y, doorDir);
        int outerMinX = innerOrigin.getX() - ElevatorShaftScanner.FRAME_MARGIN;
        int outerMaxX = innerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;
        int outerMinZ = innerOrigin.getZ() - ElevatorShaftScanner.FRAME_MARGIN;
        int outerMaxZ = innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;

        for (int x = outerMinX; x <= outerMaxX; x++)
        {
            for (int z = outerMinZ; z <= outerMaxZ; z++)
            {
                boolean edge = (x == outerMinX || x == outerMaxX || z == outerMinZ || z == outerMaxZ);
                if (!edge) continue;
                BlockPos pos = new BlockPos(x, y, z);
                if (pos.equals(doorPos)) continue;
                return pos;
            }
        }
        return null;
    }

    private static void placeDoor(Level level, BlockPos innerOrigin, int floorY, @Nonnull Direction doorDir)
    {
        int doorY = floorY + ElevatorShaftScanner.DOOR_HEIGHT_OFFSET;
        BlockPos doorLower = Objects.requireNonNull(computeDoorBase(innerOrigin, doorY, doorDir));
        BlockPos doorUpper = Objects.requireNonNull(doorLower.above());

        BlockState air = Objects.requireNonNull(Blocks.AIR.defaultBlockState());
        level.setBlock(doorLower, air, Block.UPDATE_ALL);
        level.setBlock(doorUpper, air, Block.UPDATE_ALL);

        BlockPos supportPos = Objects.requireNonNull(doorLower.below());
        if (level.getBlockState(supportPos).isAir())
        {
            level.setBlock(supportPos, frameState(), Block.UPDATE_ALL);
        }

        BlockState lower = Objects.requireNonNull(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()).defaultBlockState()
                .setValue(Objects.requireNonNull(DoorBlock.FACING), doorDir)
                .setValue(Objects.requireNonNull(DoorBlock.HINGE), DoorHingeSide.LEFT)
                .setValue(Objects.requireNonNull(DoorBlock.OPEN), false)
                .setValue(Objects.requireNonNull(DoorBlock.POWERED), false)
                .setValue(Objects.requireNonNull(DoorBlock.HALF), DoubleBlockHalf.LOWER);

        BlockState upper = lower.setValue(Objects.requireNonNull(DoorBlock.HALF), DoubleBlockHalf.UPPER);
        level.setBlock(doorLower, lower, Block.UPDATE_ALL);
        level.setBlock(Objects.requireNonNull(doorUpper), Objects.requireNonNull(upper), Block.UPDATE_ALL);
    }

    private static @Nonnull BlockPos computeDoorBase(BlockPos innerOrigin, int floorY, Direction dir)
    {
        Direction horizontal = dir.getAxis().isHorizontal() ? dir : Direction.SOUTH;
        return switch (horizontal)
        {
            case NORTH -> new BlockPos(innerOrigin.getX(), floorY, innerOrigin.getZ() - ElevatorShaftScanner.FRAME_MARGIN);
            case SOUTH ->
                    new BlockPos(innerOrigin.getX(), floorY, innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1 + ElevatorShaftScanner.FRAME_MARGIN);
            case WEST -> new BlockPos(innerOrigin.getX() - ElevatorShaftScanner.FRAME_MARGIN, floorY, innerOrigin.getZ());
            case EAST ->
                    new BlockPos(innerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1 + ElevatorShaftScanner.FRAME_MARGIN, floorY, innerOrigin.getZ());
            default ->
                    new BlockPos(innerOrigin.getX(), floorY, innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1 + ElevatorShaftScanner.FRAME_MARGIN);
        };
    }

    private static @Nonnull BlockState frameState()
    {
        Block block = Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get());
        return Objects.requireNonNull(block.defaultBlockState());
    }

    private static @Nonnull BlockState controlBlockState()
    {
        Block block = Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CONTROL_BLOCK.get());
        return Objects.requireNonNull(block.defaultBlockState());
    }

    private static @Nonnull BlockState positionFrameState()
    {
        Block block = Objects.requireNonNull(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get());
        return Objects.requireNonNull(block.defaultBlockState());
    }

    private static Component text(@Nonnull String message)
    {
        return Objects.requireNonNull(Component.literal(message));
    }
}
