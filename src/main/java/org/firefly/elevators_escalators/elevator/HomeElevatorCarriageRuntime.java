package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.firefly.elevators_escalators.EnrollEntities;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockInstance;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Runtime helper that swaps the carriage between its block footprint and entity form.
 */
public final class HomeElevatorCarriageRuntime
{
    private HomeElevatorCarriageRuntime()
    {
    }

    public static boolean requestMoveToFloor(@Nonnull ServerLevel level,
                                             @Nonnull ElevatorMultiblockInstance instance,
                                             int logicalFloor)
    {
        Objects.requireNonNull(level);
        Objects.requireNonNull(instance);
        HomeElevatorCarriageEntity entity = awaken(level, instance);
        if (entity == null)
        {
            return false;
        }

        int clampedFloor = Math.max(0, Math.min(logicalFloor, instance.floorYs().size() - 1));
        entity.resetIdleTimer();
        entity.moveToLogicalFloor(clampedFloor);

        ElevatorMultiblockSavedData data = ElevatorMultiblockSavedData.get(level);
        int floorY = instance.floorYs().isEmpty() ? instance.baseFloorY() : instance.floorYs().get(clampedFloor);
        data.setRuntimeState(instance.controllerPos(),
                ElevatorMultiblockSavedData.CarriageRuntimeState.entityActive(floorY));
        return true;
    }

    public static void restAsBlocks(@Nonnull ServerLevel level,
                                    @Nonnull ElevatorMultiblockInstance instance,
                                    int floorY)
    {
        Objects.requireNonNull(level);
        Objects.requireNonNull(instance);
        int blockBaseY = blockBaseY(floorY);
        HomeElevatorCarriageFootprint.clear(level, instance.innerOrigin(), blockBaseY);
        HomeElevatorCarriageFootprint.fillWithCarriage(level, instance.innerOrigin(), blockBaseY);
        ElevatorMultiblockSavedData.get(level).setRuntimeState(instance.controllerPos(),
                ElevatorMultiblockSavedData.CarriageRuntimeState.blocks(floorY));
    }

    public static @Nullable HomeElevatorCarriageEntity awaken(@Nonnull ServerLevel level,
                                                              @Nonnull ElevatorMultiblockInstance instance)
    {
        Objects.requireNonNull(level);
        Objects.requireNonNull(instance);
        HomeElevatorCarriageEntity existing = findLinkedEntity(level, instance.controllerPos(), instance);
        if (existing != null)
        {
            existing.resetIdleTimer();
            return existing;
        }

        ElevatorMultiblockSavedData data = ElevatorMultiblockSavedData.get(level);
        ElevatorMultiblockSavedData.CarriageRuntimeState runtimeState = data.getRuntimeState(instance.controllerPos())
                .orElse(ElevatorMultiblockSavedData.CarriageRuntimeState.blocks(instance.baseFloorY()));
        int restingFloorY = runtimeState.restingFloorY();
    int blockBaseY = blockBaseY(restingFloorY);
    HomeElevatorCarriageFootprint.clear(level, instance.innerOrigin(), blockBaseY);

        var type = EnrollEntities.HOME_ELEVATOR_CARRIAGE.get();
        HomeElevatorCarriageEntity entity = new HomeElevatorCarriageEntity(type, level);
        entity.bindToShaft(instance.controllerPos(), instance.innerOrigin());
        entity.initFloors(instance.floorYs(), instance.baseFloorY(), instance.yToLogicalFloor());

        double centerX = instance.innerOrigin().getX() + ElevatorShaftScanner.INNER_WIDTH / 2.0D;
        double centerZ = instance.innerOrigin().getZ() + ElevatorShaftScanner.INNER_DEPTH / 2.0D;
    double centerY = blockBaseY;
        entity.moveTo(centerX, centerY, centerZ, 0.0F, 0.0F);

        int logicalFloor = instance.yToLogicalFloor().getOrDefault(restingFloorY, 0);
        entity.moveToLogicalFloor(logicalFloor);
        level.addFreshEntity(entity);

        data.setRuntimeState(instance.controllerPos(),
                ElevatorMultiblockSavedData.CarriageRuntimeState.entityActive(restingFloorY));
        return entity;
    }

    private static @Nullable HomeElevatorCarriageEntity findLinkedEntity(@Nonnull ServerLevel level,
                                                                         @Nonnull BlockPos controllerPos,
                                                                         @Nonnull ElevatorMultiblockInstance instance)
    {
        AABB bounds = new AABB(instance.minCorner().getX(), instance.minCorner().getY(), instance.minCorner().getZ(),
                instance.maxCorner().getX() + 1, instance.maxCorner().getY() + 1, instance.maxCorner().getZ() + 1);
        List<HomeElevatorCarriageEntity> matches = level.getEntitiesOfClass(HomeElevatorCarriageEntity.class, bounds,
                entity -> entity.isLinkedTo(controllerPos));
        if (matches.isEmpty())
        {
            return null;
        }
        return matches.get(0);
    }

    private static int blockBaseY(int floorY)
    {
        return floorY + HomeElevatorCarriageEntity.BLOCK_BASE_OFFSET;
    }
}
