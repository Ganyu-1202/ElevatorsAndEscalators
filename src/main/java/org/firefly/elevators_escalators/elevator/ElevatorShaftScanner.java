package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.firefly.elevators_escalators.EnrollBlocks;

import java.util.*;
import java.util.stream.Collectors;



public final class ElevatorShaftScanner
{
    // 轿厢内尺寸
    public static final int INNER_WIDTH = 2;
    public static final int INNER_DEPTH = 2;
    // 外围框架扩展半径（四周一格框架）
    public static final int FRAME_MARGIN = 1;
    // 楼层上方净空（轿厢内部高度需求，与你的轿厢实体内部高度对应）
    public static final int REQUIRED_CLEAR_HEIGHT = 4;
    // 门的判定高度相对楼层的偏移（门下半部分位于楼层上方 1 格）
    public static final int DOOR_HEIGHT_OFFSET = 1;

    private ElevatorShaftScanner()
    {
    }

    public record ScanResult(List<Integer> floorYs,
                             int baseFloorY,
                             Map<Integer, Integer> yToLogicalFloor,
                             boolean topLayerHasController)
    {
    }

    public record DiagnosticReport(boolean structureReady,
                                   List<String> problems,
                                   List<String> suggestions,
                                   List<Integer> buildableFloors,
                                   Integer baseFloorY)
    {
        public boolean hasProblems()
        {
            return !problems.isEmpty();
        }
    }

    public static ScanResult scan(Level level, BlockPos controllerPos, Direction facing)
    {
        // 推导井的基准面左下角（以控制器水平投影中心向左/向后偏移）
        // 假设轿厢内腔在控制器正前下方：可根据你的控制器实际放置层微调
        BlockPos innerOrigin = computeInnerOrigin(level, controllerPos, facing); // 轿厢底面左下角
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
            return new ScanResult(List.of(), 0, Map.of(), false);
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
        boolean topLayerHasController = false;
        if (!ordered.isEmpty())
        {
            int topFloorY = ordered.get(ordered.size() - 1);
            topLayerHasController = ringHasMainController(level, innerOrigin.getX(), topFloorY, innerOrigin.getZ());
        }

        return new ScanResult(ordered, baseFloor, map, topLayerHasController);
    }

    public static DiagnosticReport diagnose(Level level, BlockPos controllerPos, Direction facing)
    {
        BlockPos innerOrigin = computeInnerOrigin(level, controllerPos, facing);
        int minY = innerOrigin.getY() - 32;
        int maxY = innerOrigin.getY() + 64;

        List<Integer> ringFloors = new ArrayList<>();
        List<Integer> partialRingFloors = new ArrayList<>();
        for (int y = minY; y <= maxY; y++)
        {
            boolean ring = ringComplete(level, innerOrigin.getX(), y, innerOrigin.getZ(), facing);
            boolean hasFramePieces = edgeHasFrame(level, innerOrigin.getX(), y, innerOrigin.getZ());
            if (ring)
            {
                ringFloors.add(y);
            }
            else if (hasFramePieces)
            {
                partialRingFloors.add(y);
            }
        }

        List<String> problems = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<Integer> buildableFloors = new ArrayList<>();
        List<Integer> floorsBadInner = new ArrayList<>();
        List<Integer> floorsObstructed = new ArrayList<>();
        List<Integer> floorsMissingDoor = new ArrayList<>();
        List<Integer> floorsWithPositionInvalid = new ArrayList<>();
        Integer baseFloorY = null;
        boolean hasAnyPositionFrame = false;

        if (ringFloors.isEmpty())
        {
            if (partialRingFloors.isEmpty())
            {
                problems.add("未检测到外围框架：请先建造 4x4 的空心框架。");
            }
            else
            {
                problems.add("以下高度检测到未闭合的框架环：" + formatFloors(partialRingFloors));
            }
            suggestions.add("在目标高度用家用电梯结构方块/定位框架补齐一圈，形成 4x4 的空心外圈。");
            return new DiagnosticReport(false, List.copyOf(problems), List.copyOf(suggestions), List.of(), null);
        }

        int topRingY = ringFloors.get(ringFloors.size() - 1);

        for (int y : ringFloors)
        {
            boolean hasPosition = hasPositionFrame(level, innerOrigin.getX(), y, innerOrigin.getZ(), facing);
            if (hasPosition)
            {
                hasAnyPositionFrame = true;
            }

            FloorInspection inspection = inspectFloor(level, innerOrigin, y, hasPosition);
            if (inspection.requireSolidInterior() && !inspection.solidInterior())
            {
                floorsBadInner.add(y);
            }
            if (!inspection.clearanceOk())
            {
                floorsObstructed.add(y);
            }

            if (inspection.isUsable())
            {
                buildableFloors.add(y);
                if (hasPosition && baseFloorY == null)
                {
                    baseFloorY = y;
                }
                if (!floorHasDoor(level, controllerPos, facing, y))
                {
                    floorsMissingDoor.add(y);
                }
            }
            else if (hasPosition)
            {
                floorsWithPositionInvalid.add(y);
            }
        }

        if (!floorsBadInner.isEmpty())
        {
            problems.add("以下楼层的内部 2x2 地面存在空气或放置了框架方块：" + formatFloors(floorsBadInner));
            suggestions.add("请用普通实心方块填满这些楼层的 2x2 地面。");
        }
        if (!floorsObstructed.isEmpty())
        {
            problems.add("以下楼层上方净空不足（需要 " + REQUIRED_CLEAR_HEIGHT + " 格空气）：" + formatFloors(floorsObstructed));
            suggestions.add("清理这些楼层上方的方块，保证轿厢有足够空间。");
        }
        if (buildableFloors.isEmpty())
        {
            problems.add("尚未找到满足地面与净空要求的楼层。");
        }
        if (!floorsMissingDoor.isEmpty())
        {
            problems.add("以下楼层缺少电梯门：" + formatFloors(floorsMissingDoor));
            suggestions.add("在这些楼层外围的一侧、楼层上方一格的位置放置一扇电梯门（下半部分）。");
        }
        if (buildableFloors.size() > 32)
        {
            problems.add("检测到 " + buildableFloors.size() + " 层可用楼层，超过最大支持 32 层。");
            suggestions.add("请减少楼层数量，使其不超过 32 层。");
        }
        if (!hasAnyPositionFrame)
        {
            problems.add("未检测到定位框架：无法确定基准层。");
            suggestions.add("在基准层的外框中替换至少一块为电梯定位框架。");
        }
        else if (baseFloorY == null)
        {
            problems.add("定位框架所在楼层未满足地板或净空要求。");
            if (!floorsWithPositionInvalid.isEmpty())
            {
                problems.add("需要修复的定位楼层：" + formatFloors(floorsWithPositionInvalid));
            }
            suggestions.add("先修复定位楼层的地板/净空，再尝试激活控制器。");
        }

        boolean topLayerHasController = ringHasMainController(level, innerOrigin.getX(), topRingY, innerOrigin.getZ());
        if (!topLayerHasController)
        {
            problems.add("最上层框架缺少主控制器。");
            suggestions.add("在最上层框架的任意一侧用主控制器替换一个框架方块。");
        }

        if (problems.isEmpty())
        {
            suggestions.add("结构满足所有要求，可直接放置控制器形成电梯。可用楼层：" + formatFloors(buildableFloors));
        }
        else if (suggestions.isEmpty())
        {
            suggestions.add("根据上述问题逐条修复后再尝试放置控制器。");
        }

        return new DiagnosticReport(problems.isEmpty(),
                List.copyOf(problems),
                List.copyOf(suggestions),
                List.copyOf(buildableFloors),
                baseFloorY);
    }

    private static BlockPos computeInnerOrigin(Level level, BlockPos controllerPos, Direction facing)
    {
        return inferInnerOriginFromRing(level, controllerPos)
                .orElse(controllerPos.below(4));
    }

    private static Optional<BlockPos> inferInnerOriginFromRing(Level level, BlockPos controllerPos)
    {
        Block start = level.getBlockState(controllerPos).getBlock();
        if (!isRingBlock(start))
        {
            return Optional.empty();
        }

        int planeY = controllerPos.getY();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(controllerPos);
        visited.add(controllerPos);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL)
            {
                BlockPos next = current.relative(dir);
                if (next.getY() != planeY || visited.contains(next)) continue;
                Block b = level.getBlockState(next).getBlock();
                if (!isRingBlock(b)) continue;
                visited.add(next);
                queue.add(next);
            }
        }

        if (visited.isEmpty())
        {
            return Optional.empty();
        }

        int minX = visited.stream().mapToInt(BlockPos::getX).min().orElse(controllerPos.getX());
        int maxX = visited.stream().mapToInt(BlockPos::getX).max().orElse(controllerPos.getX());
        int minZ = visited.stream().mapToInt(BlockPos::getZ).min().orElse(controllerPos.getZ());
        int maxZ = visited.stream().mapToInt(BlockPos::getZ).max().orElse(controllerPos.getZ());

        int expectedSpan = INNER_WIDTH + FRAME_MARGIN * 2;
        if (maxX - minX + 1 < expectedSpan || maxZ - minZ + 1 < expectedSpan)
        {
            return Optional.empty();
        }

        int originX = minX + FRAME_MARGIN;
        int originZ = minZ + FRAME_MARGIN;
        int originY = controllerPos.getY() - 4;
        return Optional.of(new BlockPos(originX, originY, originZ));
    }

    private static boolean edgeHasFrame(Level level, int originX, int y, int originZ)
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
                if (isRingBlock(b))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static FloorInspection inspectFloor(Level level, BlockPos innerOrigin, int y, boolean requireSolidInterior)
    {
        boolean solidInterior = true;
        for (int dx = 0; dx < INNER_WIDTH && solidInterior; dx++)
        {
            for (int dz = 0; dz < INNER_DEPTH; dz++)
            {
                BlockPos p = new BlockPos(innerOrigin.getX() + dx, y, innerOrigin.getZ() + dz);
                BlockState s = level.getBlockState(p);
                if (s.isAir())
                {
                    solidInterior = false;
                    break;
                }
                Block b = s.getBlock();
                if (b == EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()
                        || b == EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()
                        || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get())
                {
                    solidInterior = false;
                    break;
                }
            }
        }

        boolean clearanceOk = true;
        for (int h = 1; h <= REQUIRED_CLEAR_HEIGHT && clearanceOk; h++)
        {
            for (int dx = 0; dx < INNER_WIDTH && clearanceOk; dx++)
            {
                for (int dz = 0; dz < INNER_DEPTH; dz++)
                {
                    BlockPos p = new BlockPos(innerOrigin.getX() + dx, y + h, innerOrigin.getZ() + dz);
                    if (!level.getBlockState(p).isAir())
                    {
                        clearanceOk = false;
                        break;
                    }
                }
            }
        }

        return new FloorInspection(solidInterior, clearanceOk, requireSolidInterior);
    }

    private record FloorInspection(boolean solidInterior, boolean clearanceOk, boolean requireSolidInterior)
    {
        boolean isUsable()
        {
            return (!requireSolidInterior || solidInterior) && clearanceOk;
        }
    }

    /**
     * 检查给定楼层的外围环上是否至少存在一个电梯门（门的下半部分）
     */
    public static boolean floorHasDoor(Level level, BlockPos controllerPos, Direction facing, int y)
    {
        BlockPos innerOrigin = computeInnerOrigin(level, controllerPos, facing);

        int outerMinX = innerOrigin.getX() - FRAME_MARGIN;
        int outerMinZ = innerOrigin.getZ() - FRAME_MARGIN;
        int outerMaxX = innerOrigin.getX() + INNER_WIDTH - 1 + FRAME_MARGIN;
        int outerMaxZ = innerOrigin.getZ() + INNER_DEPTH - 1 + FRAME_MARGIN;

        for (int x = outerMinX; x <= outerMaxX; x++)
        {
            for (int z = outerMinZ; z <= outerMaxZ; z++)
            {
                boolean edge = (x == outerMinX || x == outerMaxX || z == outerMinZ || z == outerMaxZ);
                if (!edge) continue;
                BlockPos p = new BlockPos(x, y + DOOR_HEIGHT_OFFSET, z);
                BlockState bs = level.getBlockState(p);
                Block b = bs.getBlock();
                if (b == EnrollBlocks.ELEVATOR_DOOR_BLOCK.get())
                {
                    if (bs.hasProperty(DoorBlock.HALF) && bs.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isFloor(Level level, int originX, int y, int originZ, Direction facing)
    {
        // 1. 外围框架环完整
        if (!ringComplete(level, originX, y, originZ, facing)) return false;

        boolean requireSolid = hasPositionFrame(level, originX, y, originZ, facing);
        FloorInspection inspection = inspectFloor(level, new BlockPos(originX, y, originZ), y, requireSolid);
        return inspection.isUsable();
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
                if (!isRingBlock(b))
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
                if (isPositionFrameBlock(b))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean ringHasMainController(Level level, int originX, int y, int originZ)
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
                if (b == EnrollBlocks.MAIN_CONTROLLER_BLOCK.get())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isRingBlock(Block b)
    {
        return b == EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()
                || b == EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()
                || b == EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()
                || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()
        || b == EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()
        || b == EnrollBlocks.MAIN_CONTROLLER_BLOCK.get()
                || isPositionFrameBlock(b);
    }

    private static boolean isPositionFrameBlock(Block b)
    {
        return b == EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()
                || b == EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get();
    }

    private static String formatFloors(List<Integer> floors)
    {
        if (floors.isEmpty())
        {
            return "无";
        }
        String joined = floors.stream()
                .sorted()
                .map(String::valueOf)
                .limit(8)
                .collect(Collectors.joining(", "));
        if (floors.size() > 8)
        {
            joined += " 等 " + floors.size() + " 层";
        }
        return joined;
    }
}