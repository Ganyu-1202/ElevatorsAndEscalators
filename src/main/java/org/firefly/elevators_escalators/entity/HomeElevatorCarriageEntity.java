package org.firefly.elevators_escalators.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class HomeElevatorCarriageEntity extends Entity
{
    private static final String NBT_LIGHT_KEY = "CarriageLight";
    private static final String NBT_POWER_KEY = "CarriagePowered";

    private List<Integer> floorYs = List.of();
    private int baseFloorY;
    private Map<Integer, Integer> yToLogicalFloor = Map.of();
    private double targetY;
    private static final double MOVE_SPEED = 0.1; // 每 tick 垂直速度，可调

    private static final EntityDataAccessor<Boolean> DATA_POWERED =
            SynchedEntityData.defineId(HomeElevatorCarriageEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LIGHT =
            SynchedEntityData.defineId(HomeElevatorCarriageEntity.class, EntityDataSerializers.INT);

    public HomeElevatorCarriageEntity(EntityType<? extends HomeElevatorCarriageEntity> entityType, Level level)
    {
        super(entityType, level);
        this.noPhysics = true;
    }

    public HomeElevatorCarriageEntity(EntityType<? extends HomeElevatorCarriageEntity> entityType, Level level, boolean isPoweredOn)
    {
        this(entityType, level);
        this.setPoweredOn(isPoweredOn);
        this.setInnerLightLevel(isPoweredOn ? 10 : 1);
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder synchedDataBuilder)
    {
        synchedDataBuilder.define(DATA_POWERED, false);
        synchedDataBuilder.define(DATA_LIGHT, 1);
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound)
    {
        compound.putBoolean(NBT_POWER_KEY, this.isPoweredOn());
        compound.putInt(NBT_LIGHT_KEY, this.getInnerLightLevel());
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound)
    {
        if (compound.contains(NBT_POWER_KEY))
        {
            this.setPoweredOn(compound.getBoolean(NBT_POWER_KEY));
        }
        if (compound.contains(NBT_LIGHT_KEY))
        {
            this.setInnerLightLevel(compound.getInt(NBT_LIGHT_KEY));
        }
    }

    @Override
    public @Nonnull Packet<ClientGamePacketListener> getAddEntityPacket(@Nonnull ServerEntity serverEntity)
    {
        return super.getAddEntityPacket(serverEntity);
    }

    public void initFloors(List<Integer> floors, int baseY, Map<Integer,Integer> map) {
        this.floorYs = new ArrayList<>(floors);
        this.baseFloorY = baseY;
        this.yToLogicalFloor = new HashMap<>(map);
        this.targetY = getY(); // 初始停在当前位置
    }

    public int getCurrentLogicalFloor() {
        int closestY = findClosestFloorY(getY());
        return yToLogicalFloor.getOrDefault(closestY, 0);
    }

    private int findClosestFloorY(double y) {
        int best = floorYs.get(0);
        double bestDist = Math.abs(y - best);
        for (int fy : floorYs) {
            double d = Math.abs(y - fy);
            if (d < bestDist) { best = fy; bestDist = d; }
        }
        return best;
    }

    public void moveToLogicalFloor(int logicalFloor) {
        // 找到目标绝对 Y
        for (int fy : floorYs) {
            int lf = yToLogicalFloor.getOrDefault(fy, 0);
            if (lf == logicalFloor) {
                this.targetY = fy + 1.0; // +1: 轿厢平台相对 floorY 的内部高度偏移
                break;
            }
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        
        double y = getY();
        if (Math.abs(targetY - y) > 0.01) {
            double dir = Math.signum(targetY - y);
            double newY = y + dir * MOVE_SPEED;
            if ((dir > 0 && newY > targetY) || (dir < 0 && newY < targetY)) {
                newY = targetY;
            }
            setPos(getX(), newY, getZ());
            // 同步乘客
            for (var passenger : getPassengers()) {
                passenger.setPos(passenger.getX(), newY, passenger.getZ());
            }
        }
    }

    @Override
    public @Nonnull EntityDimensions getDimensions(@Nonnull Pose pose)
    {
        return EntityDimensions.fixed(2.0F, 2.0F);
    }

    public boolean isPoweredOn()
    {
        return this.entityData.get(DATA_POWERED);
    }

    public void setPoweredOn(boolean powered)
    {
        this.entityData.set(DATA_POWERED, powered);
    }

    /**
     * 获取轿厢内部光照等级（0-15）
     */
    public int getInnerLightLevel()
    {
        return this.entityData.get(DATA_LIGHT);
    }

    /**
     * 设置轿厢内部光照等级（0-15）
     */
    public void setInnerLightLevel(int level)
    {
        this.entityData.set(DATA_LIGHT, Math.max(0, Math.min(15, level)));
    }
}