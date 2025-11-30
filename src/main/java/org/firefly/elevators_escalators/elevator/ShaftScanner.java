package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.EnrollBlocks;

import java.util.*;

public final class ShaftScanner
{
    // 轿厢内尺寸
    public static final int INNER_WIDTH = 2;
    public static final int INNER_DEPTH = 2;
    // 外围框架扩展半径（四周一格框架）
    public static final int FRAME_MARGIN = 1;
    // 楼层上方净空（轿厢内部高度需求，与你的轿厢实体内部高度对应）
    public static final int REQUIRED_CLEAR_HEIGHT = 4;

    private ShaftScanner()
    {
    }

    public record ScanResult(List<Integer> floorYs, int baseFloorY, Map<Integer, Integer> yToLogicalFloor)
    {
    }

    public static ScanResult scan(Level level, BlockPos controllerPos, Direction facing)
    {
        // 推导井的基准面左下角（以控制器水平投影中心向左/向后偏移）
        Direction right = facing.getClockWise();
        // 假设轿厢内腔在控制器正前下方：可根据你的控制器实际放置层微调
        BlockPos innerOrigin = controllerPos.relative(facing, 0).relative(right, 0).below(4); // 轿厢底面左下角
        int minY = innerOrigin.getY() - 32;
        int maxY = innerOrigin.getY() + 64;

        List<Integer> rawFloors = new ArrayList<>();
        Integer baseFloor = null;

        for (int y = minY; y <= maxY; y++)
        {
            if (isFloor(level, innerOrigin.getX(), y, innerOrigin.getZ(), facing))
            {
                rawFloors.add(y);
                if (hasPositionFrame(level, innerOrigin.getX(), y, innerOrigin.getZ(), facing))
                {
                    baseFloor = y;
                }
            }
        }

        if (rawFloors.isEmpty() || baseFloor == null)
        {
            return new ScanResult(List.of(), 0, Map.of());
        }

        Collections.sort(rawFloors);
        Map<Integer, Integer> map = new HashMap<>();
        List<Integer> ordered = new ArrayList<>();
        for (int y : rawFloors)
        {
            int logical = y - baseFloor;
            map.put(y, logical);
            ordered.add(y);
        }
        return new ScanResult(ordered, baseFloor, map);
    }

    private static boolean isFloor(Level level, int originX, int y, int originZ, Direction facing)
    {
        // 1. 外围框架环完整
        if (!ringComplete(level, originX, y, originZ, facing)) return false;
        // 2. 内部 4×4 地板全部非空气（且不能是框架 / 定位）
        for (int dx = 0; dx < INNER_WIDTH; dx++)
        {
            for (int dz = 0; dz < INNER_DEPTH; dz++)
            {
                BlockPos p = new BlockPos(originX + dx, y, originZ + dz);
                BlockState s = level.getBlockState(p);
                if (s.isAir()) return false;
                Block b = s.getBlock();
                if (b == EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()
                        || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get())
                {
                    return false;
                }
            }
        }
        // 3. 上方净空
        for (int h = 1; h <= REQUIRED_CLEAR_HEIGHT; h++)
        {
            for (int dx = 0; dx < INNER_WIDTH; dx++)
            {
                for (int dz = 0; dz < INNER_DEPTH; dz++)
                {
                    BlockPos p = new BlockPos(originX + dx, y + h, originZ + dz);
                    if (!level.getBlockState(p).isAir())
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean ringComplete(Level level, int originX, int y, int originZ, Direction facing)
    {
        int outerMinX = originX - FRAME_MARGIN;
        int outerMinZ = originZ - FRAME_MARGIN;
        int outerMaxX = originX + INNER_WIDTH - 1 + FRAME_MARGIN;
        int outerMaxZ = originZ + INNER_DEPTH - 1 + FRAME_MARGIN;

        for (int x = outerMinX; x <= outerMaxX; x++)
        {
            for (int z = outerMinZ; z <= outerMaxZ; z++)
            {
                boolean edge = (x == outerMinX || x == outerMaxX || z == outerMinZ || z == outerMaxZ);
                if (!edge) continue; // 只看外围一圈
                BlockPos p = new BlockPos(x, y, z);
                Block b = level.getBlockState(p).getBlock();
                if (!(b == EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()
                        || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasPositionFrame(Level level, int originX, int y, int originZ, Direction facing)
    {
        int outerMinX = originX - FRAME_MARGIN;
        int outerMinZ = originZ - FRAME_MARGIN;
        int outerMaxX = originX + INNER_WIDTH - 1 + FRAME_MARGIN;
        int outerMaxZ = originZ + INNER_DEPTH - 1 + FRAME_MARGIN;

        for (int x = outerMinX; x <= outerMaxX; x++)
        {
            for (int z = outerMinZ; z <= outerMaxZ; z++)
            {
                boolean edge = (x == outerMinX || x == outerMaxX || z == outerMinZ || z == outerMaxZ);
                if (!edge) continue;
                BlockPos p = new BlockPos(x, y, z);
                Block b = level.getBlockState(p).getBlock();
                if (b == EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()
                        || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get())
                {
                    return true;
                }
            }
        }
        return false;
    }
}