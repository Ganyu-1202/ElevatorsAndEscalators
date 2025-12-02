package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import org.firefly.elevators_escalators.ControllerBlock;
import org.firefly.elevators_escalators.EnrollBlocks;
import org.firefly.elevators_escalators.EnrollEntities;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockInstance;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HomeElevatorAssembler
{
    private static final int MAX_FLOORS = 32;

    private HomeElevatorAssembler()
    {
    }

    public static boolean tryAssemble(@Nonnull Level level,
                                      @Nonnull BlockPos controllerPos,
                                      @Nonnull Direction facing,
                                      @Nullable Player player)
    {
        ElevatorShaftScanner.ScanResult scanResult = ElevatorShaftScanner.scan(level, controllerPos, facing);
        List<Integer> floorYs = scanResult.floorYs();
        if (floorYs.isEmpty())
        {
            notifyPlayer(player, "结构未完成：请先使用主控制器确认结构无误。", true);
            return false;
        }

        if (floorYs.size() > MAX_FLOORS)
        {
            notifyPlayer(player, "楼层数量超过最大支持的 " + MAX_FLOORS + " 层。", true);
            return false;
        }

        if (!scanResult.topLayerHasController())
        {
            notifyPlayer(player, "最上层框架需要放置主控制器才能装配轿厢。", true);
            return false;
        }

        for (int fy : floorYs)
        {
            if (!ElevatorShaftScanner.floorHasDoor(level, controllerPos, facing, fy))
            {
                notifyPlayer(player, "每一层都需要至少一扇电梯门。", true);
                return false;
            }
        }

        HomeElevatorCarriageEntity entity = assemble(level, controllerPos, facing, scanResult);
        notifyPlayer(player, "成功装配家用电梯轿厢！", false);
        if (player != null && entity != null)
        {
            List<String> labels = entity.getFloorLabels();
            if (!labels.isEmpty())
            {
                notifyPlayer(player,
                        "楼层命名：" + String.join(", ", labels) + "（默认停靠 " + entity.getFloorName(entity.getClosestLogicalFloor()) + "）",
                        false);
            }
        }
        return true;
    }

    private static HomeElevatorCarriageEntity assemble(Level level,
                                                       BlockPos controllerPos,
                                                       Direction facing,
                                                       ElevatorShaftScanner.ScanResult scanResult)
    {
    BlockPos innerOrigin = Objects.requireNonNull(scanResult.innerOrigin());
    List<Integer> floorYs = Objects.requireNonNull(scanResult.floorYs());
    Map<Integer, Integer> floorMap = Objects.requireNonNull(scanResult.yToLogicalFloor());

    int minX = innerOrigin.getX();
    int maxX = innerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1;
    int minZ = innerOrigin.getZ();
    int maxZ = innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1;
        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;
    int spawnFloorY = pickInitialFloor(floorYs, scanResult.baseFloorY());
    double centerY = spawnFloorY + HomeElevatorCarriageEntity.BLOCK_BASE_OFFSET;

    int minFloor = floorYs.get(0);
    int maxFloor = floorYs.get(floorYs.size() - 1);
        int clearanceTop = maxFloor + ElevatorShaftScanner.REQUIRED_CLEAR_HEIGHT + 1;

        clearCarInterior(level, innerOrigin, minFloor, clearanceTop);
        flagControllerAsAssembled(level, controllerPos, facing);

        var type = EnrollEntities.HOME_ELEVATOR_CARRIAGE.get();
        HomeElevatorCarriageEntity entity = new HomeElevatorCarriageEntity(type, level);
    entity.initFloors(floorYs, scanResult.baseFloorY(), floorMap);
    entity.bindToShaft(Objects.requireNonNull(controllerPos), Objects.requireNonNull(innerOrigin));
        entity.moveTo(centerX, centerY, centerZ, 0f, 0f);
        level.addFreshEntity(entity);

    registerMultiblock(level, controllerPos, innerOrigin, minFloor, clearanceTop, floorMap, floorYs, facing, scanResult.baseFloorY(), spawnFloorY);
    return entity;
    }

    private static void clearCarInterior(Level level,
                                         BlockPos innerOrigin,
                                         int minY,
                                         int maxY)
    {
        int innerMinX = innerOrigin.getX();
        int innerMaxX = innerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1;
        int innerMinZ = innerOrigin.getZ();
        int innerMaxZ = innerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1;

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = innerMinX; x <= innerMaxX; x++)
            {
                for (int z = innerMinZ; z <= innerMaxZ; z++)
                {
                    BlockPos target = new BlockPos(x, y, z);
                    level.removeBlock(target, false);
                }
            }
        }
    }

    private static void flagControllerAsAssembled(Level level, BlockPos controllerPos, Direction facing)
    {
    BlockState state = level.getBlockState(Objects.requireNonNull(controllerPos));
        if (state.getBlock() != EnrollBlocks.MAIN_CONTROLLER_BLOCK.get())
        {
            return;
        }

    BlockState updated = state
        .setValue(Objects.requireNonNull(ControllerBlock.ASSEMBLED), true)
        .setValue(Objects.requireNonNull(ControllerBlock.FACING), Objects.requireNonNull(facing));
    level.setBlock(controllerPos, Objects.requireNonNull(updated), Block.UPDATE_ALL);
    }

    @SuppressWarnings("null")
    private static void registerMultiblock(Level level,
                                           BlockPos controllerPos,
                                           BlockPos innerOrigin,
                                           int minY,
                                           int maxY,
                                           Map<Integer, Integer> floorMap,
                                           List<Integer> floorYs,
                                           Direction facing,
                                           int baseFloorY,
                                           int initialRestingFloorY)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

    BlockPos safeController = Objects.requireNonNull(controllerPos);
    BlockPos safeInnerOrigin = Objects.requireNonNull(innerOrigin);
    Map<Integer, Integer> safeFloorMap = Map.copyOf(Objects.requireNonNull(floorMap));
    List<Integer> safeFloors = List.copyOf(Objects.requireNonNull(floorYs));

    int outerMinX = safeInnerOrigin.getX() - ElevatorShaftScanner.FRAME_MARGIN;
    int outerMinZ = safeInnerOrigin.getZ() - ElevatorShaftScanner.FRAME_MARGIN;
    int outerMaxX = safeInnerOrigin.getX() + ElevatorShaftScanner.INNER_WIDTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;
    int outerMaxZ = safeInnerOrigin.getZ() + ElevatorShaftScanner.INNER_DEPTH - 1 + ElevatorShaftScanner.FRAME_MARGIN;

    BlockPos minCorner = new BlockPos(outerMinX, minY, outerMinZ);
    BlockPos maxCorner = new BlockPos(outerMaxX, maxY, outerMaxZ);
    @Nonnull BlockPos controllerCopy = Objects.requireNonNull(safeController).immutable();
    @Nonnull BlockPos innerOriginCopy = Objects.requireNonNull(safeInnerOrigin).immutable();
    @Nonnull List<Integer> floorsCopy = List.copyOf(Objects.requireNonNull(safeFloors));
    @Nonnull Map<Integer, Integer> floorMapCopy = Map.copyOf(Objects.requireNonNull(safeFloorMap));

    ElevatorMultiblockInstance instance = new ElevatorMultiblockInstance(
        controllerCopy,
        innerOriginCopy,
        minCorner,
        maxCorner,
        Objects.requireNonNull(facing),
        floorsCopy,
        floorMapCopy,
        baseFloorY);

        ElevatorMultiblockSavedData data = ElevatorMultiblockSavedData.get(serverLevel);
        data.add(instance);
        data.setRuntimeState(instance.controllerPos(), ElevatorMultiblockSavedData.CarriageRuntimeState.entityActive(initialRestingFloorY));
    }

    private static void notifyPlayer(@Nullable Player player, @Nonnull String message, boolean isError)
    {
        if (player != null)
        {
            player.displayClientMessage(Objects.requireNonNull(Component.literal(message)), isError);
        }
    }

    private static int pickInitialFloor(@Nonnull List<Integer> floorYs, int baseFloorY)
    {
        if (floorYs.isEmpty())
        {
            throw new IllegalArgumentException("No floors available for elevator assembly.");
        }
        int baseIndex = floorYs.indexOf(baseFloorY);
        if (baseIndex < 0)
        {
            baseIndex = 0;
        }
        return floorYs.get(baseIndex);
    }
}
