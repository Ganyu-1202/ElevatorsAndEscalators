package org.firefly.elevators_escalators.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.firefly.elevators_escalators.elevator.HomeElevatorCarriageRuntime;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockInstance;
import org.firefly.elevators_escalators.elevator.multiblock.ElevatorMultiblockSavedData;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeElevatorCarriageEntity extends Entity
{
    private static final String NBT_LIGHT_KEY = "CarriageLight";
    private static final String NBT_POWER_KEY = "CarriagePowered";
    private static final String NBT_TARGET_Y_KEY = "CarriageTargetY";
    private static final String NBT_FLOORS_KEY = "CarriageFloors";
    private static final String NBT_BASE_FLOOR_KEY = "CarriageBaseFloor";
    private static final String NBT_CONTROLLER_KEY = "CarriageController";
    private static final String NBT_INNER_ORIGIN_KEY = "CarriageInnerOrigin";

    private static final double MOVE_SPEED = 0.12D;
    private static final double CABIN_HEIGHT = 2.0D;
    private static final double CABIN_HALF_WIDTH = 1.0D;
    private static final double CABIN_WALL_MARGIN = 0.25D;
    public static final int BLOCK_BASE_OFFSET = 1;

    private static final EntityDataAccessor<Boolean> DATA_POWERED = Objects.requireNonNull(
        SynchedEntityData.defineId(HomeElevatorCarriageEntity.class,
            Objects.requireNonNull(EntityDataSerializers.BOOLEAN)));
    private static final EntityDataAccessor<Integer> DATA_LIGHT = Objects.requireNonNull(
        SynchedEntityData.defineId(HomeElevatorCarriageEntity.class,
            Objects.requireNonNull(EntityDataSerializers.INT)));
        private static final EntityDataAccessor<Float> DATA_TARGET_Y = Objects.requireNonNull(
            SynchedEntityData.defineId(HomeElevatorCarriageEntity.class,
                Objects.requireNonNull(EntityDataSerializers.FLOAT)));

    private List<Integer> floorYs = List.of();
    private Map<Integer, Integer> yToLogicalFloor = Map.of();
    private List<String> floorLabels = List.of();
    private int baseFloorY;
    private int baseFloorIndex;
    private double targetY;
    private boolean needsSnapToTarget = false;
    private BlockPos controllerPos = BlockPos.ZERO;
    private BlockPos innerOrigin = BlockPos.ZERO;
    private int idleTicks;

    private static final int BLOCK_IDLE_TICKS = 10;

    public HomeElevatorCarriageEntity(EntityType<? extends HomeElevatorCarriageEntity> type, Level level)
    {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(true);
    this.targetY = this.getY();
    }

    public HomeElevatorCarriageEntity(EntityType<? extends HomeElevatorCarriageEntity> type, Level level, boolean powered)
    {
        this(type, level);
        this.setPoweredOn(powered);
        this.setInnerLightLevel(powered ? 12 : 2);
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder builder)
    {
            builder.define(Objects.requireNonNull(DATA_POWERED), false);
    builder.define(Objects.requireNonNull(DATA_LIGHT), 1);
            builder.define(Objects.requireNonNull(DATA_TARGET_Y), 0.0F);
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag)
    {
        tag.putBoolean(NBT_POWER_KEY, this.isPoweredOn());
        tag.putInt(NBT_LIGHT_KEY, this.getInnerLightLevel());
        tag.putDouble(NBT_TARGET_Y_KEY, this.targetY);
        tag.putInt(NBT_BASE_FLOOR_KEY, this.baseFloorY);
        if (controllerPos != null)
        {
            tag.putLong(NBT_CONTROLLER_KEY, controllerPos.asLong());
        }
        if (innerOrigin != null)
        {
            tag.putLong(NBT_INNER_ORIGIN_KEY, innerOrigin.asLong());
        }
        if (!floorYs.isEmpty())
        {
            tag.putIntArray(NBT_FLOORS_KEY, floorYs);
        }
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag)
    {
        double savedTarget = tag.getDouble(NBT_TARGET_Y_KEY);
        if (tag.contains(NBT_POWER_KEY))
        {
            this.setPoweredOn(tag.getBoolean(NBT_POWER_KEY));
        }
        if (tag.contains(NBT_LIGHT_KEY))
        {
            this.setInnerLightLevel(tag.getInt(NBT_LIGHT_KEY));
        }
        this.baseFloorY = tag.getInt(NBT_BASE_FLOOR_KEY);
        if (tag.contains(NBT_CONTROLLER_KEY))
        {
            this.controllerPos = BlockPos.of(tag.getLong(NBT_CONTROLLER_KEY));
        }
        if (tag.contains(NBT_INNER_ORIGIN_KEY))
        {
            this.innerOrigin = BlockPos.of(tag.getLong(NBT_INNER_ORIGIN_KEY));
        }
        if (tag.contains(NBT_FLOORS_KEY))
        {
            this.setFloors(tag.getIntArray(NBT_FLOORS_KEY), baseFloorY);
        }
            updateTargetY(savedTarget);
            scheduleSnapToTarget();
    }

    public void setFloors(int[] floors, int baseY)
    {
        if (floors.length == 0)
        {
            this.initFloors(List.of(), baseY, Map.of());
            return;
        }
        List<Integer> floorList = Arrays.stream(floors).boxed().toList();
        initFloors(floorList, baseY, buildIndexMap(floorList));
    }

    public void initFloors(List<Integer> floors,
                           int baseY,
                           Map<Integer, Integer> mapping)
    {
        this.baseFloorY = baseY;
        this.floorYs = List.copyOf(Objects.requireNonNull(floors));
        this.yToLogicalFloor = Map.copyOf(Objects.requireNonNull(mapping));
        rebuildFloorMetadata();
        scheduleSnapToTarget();
    }

    public void bindToShaft(@Nonnull BlockPos controllerPos, @Nonnull BlockPos innerOrigin)
    {
        this.controllerPos = Objects.requireNonNull(controllerPos).immutable();
        this.innerOrigin = Objects.requireNonNull(innerOrigin).immutable();
    }

    public boolean isLinkedTo(@Nonnull BlockPos controllerPos)
    {
        return Objects.equals(this.controllerPos, controllerPos);
    }

    public void resetIdleTimer()
    {
        this.idleTicks = 0;
    }

    private static Map<Integer, Integer> buildIndexMap(List<Integer> floors)
    {
        Map<Integer, Integer> mapping = new LinkedHashMap<>();
        for (int i = 0; i < floors.size(); i++)
        {
            mapping.put(floors.get(i), i);
        }
        return mapping;
    }

    private void rebuildFloorMetadata()
    {
        if (this.floorYs.isEmpty())
        {
            this.floorLabels = List.of();
            this.baseFloorIndex = 0;
            updateTargetY(this.baseFloorY + BLOCK_BASE_OFFSET);
            return;
        }

        int index = this.floorYs.indexOf(this.baseFloorY);
        if (index < 0)
        {
            index = 0;
        }
        this.baseFloorIndex = index;

        List<String> labels = new ArrayList<>(this.floorYs.size());
        for (int i = 0; i < this.floorYs.size(); i++)
        {
            if (i < index)
            {
                labels.add("B" + (index - i));
            }
            else
            {
                labels.add((i - index + 1) + "F");
            }
        }
        this.floorLabels = List.copyOf(labels);
    updateTargetY(this.floorYs.get(this.baseFloorIndex) + BLOCK_BASE_OFFSET);
    }

    private void scheduleSnapToTarget()
    {
        this.needsSnapToTarget = true;
    }

    public void moveToLogicalFloor(int logicalFloor)
    {
        resetIdleTimer();
        if (floorYs.isEmpty())
        {
            updateTargetY(baseFloorY + BLOCK_BASE_OFFSET);
            return;
        }
        int safeIndex = Math.max(0, Math.min(logicalFloor, floorYs.size() - 1));
        updateTargetY(floorYs.get(safeIndex) + BLOCK_BASE_OFFSET);
        scheduleSnapToTarget();
    }

    private void updateTargetY(double newTarget)
    {
        this.targetY = newTarget;
        Level level = level();
        if (level != null && !level.isClientSide)
        {
            this.entityData.set(Objects.requireNonNull(DATA_TARGET_Y), (float) newTarget);
        }
    }

    @Override
    public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> key)
    {
        super.onSyncedDataUpdated(key);
        if (Objects.equals(key, DATA_TARGET_Y))
        {
            this.targetY = this.entityData.get(Objects.requireNonNull(DATA_TARGET_Y));
            scheduleSnapToTarget();
        }
    }

    public int getClosestLogicalFloor()
    {
        if (floorYs.isEmpty())
        {
            return 0;
        }
    int approximateY = Mth.floor(getY() - BLOCK_BASE_OFFSET);
        Integer mapped = yToLogicalFloor.get(approximateY);
        if (mapped != null && mapped >= 0 && mapped < floorYs.size())
        {
            return mapped;
        }

    double cabinBase = getY() - BLOCK_BASE_OFFSET;
        double bestDelta = Double.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < floorYs.size(); i++)
        {
            double delta = Math.abs(floorYs.get(i) - cabinBase);
            if (delta < bestDelta)
            {
                bestDelta = delta;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public List<String> getFloorLabels()
    {
        return floorLabels;
    }

    public String getFloorName(int logicalFloor)
    {
        if (floorLabels.isEmpty())
        {
            return "1F";
        }
        int safeIndex = Math.max(0, Math.min(logicalFloor, floorLabels.size() - 1));
        return floorLabels.get(safeIndex);
    }

    @Override
    public void tick()
    {
        super.tick();

        double currentY = getY();
        double delta = 0;
        if (needsSnapToTarget)
        {
            setPos(getX(), targetY, getZ());
            needsSnapToTarget = false;
        }
        else
        {
            double distance = targetY - currentY;
            if (Math.abs(distance) > 0.01D)
            {
                double direction = Math.signum(distance);
                double newY = currentY + direction * MOVE_SPEED;
                if ((direction > 0 && newY > targetY) || (direction < 0 && newY < targetY))
                {
                    newY = targetY;
                }
                delta = newY - currentY;
                setPos(getX(), newY, getZ());
            }
            else if (Math.abs(distance) > 1.0E-4)
            {
                setPos(getX(), targetY, getZ());
            }
        }

        if (!level().isClientSide)
        {
            stabilizeOccupants(delta);
            handleIdleState(delta);
        }
    }

    private void stabilizeOccupants(double deltaY)
    {
        AABB cabin = getCabinAabb();
        List<LivingEntity> occupants = level().getEntitiesOfClass(LivingEntity.class, cabin, LivingEntity::isAlive);
        boolean moving = Math.abs(deltaY) > 0.0005D;
        double minInnerX = getX() - CABIN_HALF_WIDTH + CABIN_WALL_MARGIN;
        double maxInnerX = getX() + CABIN_HALF_WIDTH - CABIN_WALL_MARGIN;
        double minInnerZ = getZ() - CABIN_HALF_WIDTH + CABIN_WALL_MARGIN;
        double maxInnerZ = getZ() + CABIN_HALF_WIDTH - CABIN_WALL_MARGIN;

        for (LivingEntity entity : occupants)
        {
            if (Math.abs(deltaY) > 0)
            {
                entity.move(MoverType.SELF, new Vec3(0, deltaY, 0));
            }
            double floorY = getY();
            if (entity.getY() < floorY)
            {
                entity.setPos(entity.getX(), floorY, entity.getZ());
            }
            if (moving)
            {
                double clampedX = Mth.clamp(entity.getX(), minInnerX, maxInnerX);
                double clampedZ = Mth.clamp(entity.getZ(), minInnerZ, maxInnerZ);
                if (Math.abs(clampedX - entity.getX()) > 1.0E-4 || Math.abs(clampedZ - entity.getZ()) > 1.0E-4)
                {
                    entity.setPos(clampedX, entity.getY(), clampedZ);
                }
            }
            entity.fallDistance = 0;
        }
    }

    private void handleIdleState(double deltaY)
    {
        if (Math.abs(deltaY) > 0.0005D)
        {
            idleTicks = 0;
            return;
        }
        idleTicks++;
        if (idleTicks >= BLOCK_IDLE_TICKS)
        {
            idleTicks = 0;
            enterBlockIdle();
        }
    }

    private void enterBlockIdle()
    {
        Level level = level();
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }
        if (controllerPos == BlockPos.ZERO || innerOrigin == BlockPos.ZERO)
        {
            return;
        }
    ElevatorMultiblockInstance instance = ElevatorMultiblockSavedData.get(serverLevel)
        .getByController(Objects.requireNonNull(controllerPos))
                .orElse(null);
        if (instance == null)
        {
            return;
        }

        int logicalFloor = getClosestLogicalFloor();
        int safeIndex = floorYs.isEmpty() ? 0 : Math.max(0, Math.min(logicalFloor, floorYs.size() - 1));
        int floorY = floorYs.isEmpty() ? baseFloorY : floorYs.get(safeIndex);
        settleOccupantsOnFloor(floorY);
        HomeElevatorCarriageRuntime.restAsBlocks(serverLevel, instance, floorY);
        this.discard();
    }

    private void settleOccupantsOnFloor(int floorY)
    {
        Level level = level();
        if (level == null)
        {
            return;
        }
    double surfaceY = floorY + BLOCK_BASE_OFFSET;
        AABB cabin = getCabinAabb();
        List<LivingEntity> occupants = level.getEntitiesOfClass(LivingEntity.class, cabin, LivingEntity::isAlive);
        for (LivingEntity entity : occupants)
        {
            entity.setPos(entity.getX(), surfaceY, entity.getZ());
            entity.fallDistance = 0;
        }
    }

    private @Nonnull AABB getCabinAabb()
    {
        double minX = getX() - CABIN_HALF_WIDTH;
        double maxX = getX() + CABIN_HALF_WIDTH;
        double minZ = getZ() - CABIN_HALF_WIDTH;
        double maxZ = getZ() + CABIN_HALF_WIDTH;
        double minY = getY();
        double maxY = getY() + CABIN_HEIGHT;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public @Nonnull EntityDimensions getDimensions(@Nonnull Pose pose)
    {
        return Objects.requireNonNull(EntityDimensions.fixed((float) (CABIN_HALF_WIDTH * 2), (float) CABIN_HEIGHT));
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean isNoGravity()
    {
        return true;
    }

    public boolean isPoweredOn()
    {
        return this.entityData.get(Objects.requireNonNull(DATA_POWERED));
    }

    public void setPoweredOn(boolean powered)
    {
        this.entityData.set(Objects.requireNonNull(DATA_POWERED), powered);
    }

    public int getInnerLightLevel()
    {
        return this.entityData.get(Objects.requireNonNull(DATA_LIGHT));
    }

    public void setInnerLightLevel(int level)
    {
        this.entityData.set(Objects.requireNonNull(DATA_LIGHT), Math.max(0, Math.min(15, level)));
    }

}