package org.firefly.elevators_escalators.elevator.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable snapshot describing an assembled elevator shaft multiblock.
 */
public final class ElevatorMultiblockInstance
{
    private static final String TAG_CONTROLLER = "controller";
    private static final String TAG_INNER_ORIGIN = "innerOrigin";
    private static final String TAG_MIN = "min";
    private static final String TAG_MAX = "max";
    private static final String TAG_FACING = "facing";
    private static final String TAG_FLOORS = "floors";
    private static final String TAG_FLOOR_MAP = "floorMap";
    private static final String TAG_BASE_FLOOR_Y = "baseFloorY";
    private static final String TAG_FLOOR_Y = "y";
    private static final String TAG_FLOOR_LOGICAL = "logical";

    private final BlockPos controllerPos;
    private final BlockPos innerOrigin;
    private final BlockPos minCorner;
    private final BlockPos maxCorner;
    private final Direction facing;
    private final List<Integer> floorYs;
    private final Map<Integer, Integer> yToLogicalFloor;
    private final int baseFloorY;

    public ElevatorMultiblockInstance(@Nonnull BlockPos controllerPos,
                                      @Nonnull BlockPos innerOrigin,
                                      @Nonnull BlockPos minCorner,
                                      @Nonnull BlockPos maxCorner,
                                      @Nonnull Direction facing,
                                      @Nonnull List<Integer> floorYs,
                                      @Nonnull Map<Integer, Integer> yToLogicalFloor,
                                      int baseFloorY)
    {
        this.controllerPos = Objects.requireNonNull(controllerPos);
        this.innerOrigin = Objects.requireNonNull(innerOrigin);
        this.minCorner = Objects.requireNonNull(minCorner);
        this.maxCorner = Objects.requireNonNull(maxCorner);
        this.facing = Objects.requireNonNull(facing);
        this.floorYs = List.copyOf(Objects.requireNonNull(floorYs));
        this.yToLogicalFloor = Map.copyOf(Objects.requireNonNull(yToLogicalFloor));
        this.baseFloorY = baseFloorY;
    }

    public @Nonnull BlockPos controllerPos()
    {
    return Objects.requireNonNull(controllerPos);
    }

    public @Nonnull BlockPos innerOrigin()
    {
    return Objects.requireNonNull(innerOrigin);
    }

    public @Nonnull BlockPos minCorner()
    {
    return Objects.requireNonNull(minCorner);
    }

    public @Nonnull BlockPos maxCorner()
    {
    return Objects.requireNonNull(maxCorner);
    }

    public @Nonnull Direction facing()
    {
    return Objects.requireNonNull(facing);
    }

    public @Nonnull List<Integer> floorYs()
    {
    return Objects.requireNonNull(floorYs);
    }

    public @Nonnull Map<Integer, Integer> yToLogicalFloor()
    {
    return Objects.requireNonNull(yToLogicalFloor);
    }

    public int baseFloorY()
    {
    return baseFloorY;
    }

    public boolean contains(@Nonnull BlockPos pos)
    {
        return pos.getX() >= minCorner.getX() && pos.getX() <= maxCorner.getX()
                && pos.getY() >= minCorner.getY() && pos.getY() <= maxCorner.getY()
                && pos.getZ() >= minCorner.getZ() && pos.getZ() <= maxCorner.getZ();
    }

    public @Nonnull CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_CONTROLLER, controllerPos.asLong());
        tag.putLong(TAG_INNER_ORIGIN, innerOrigin.asLong());
        tag.putLong(TAG_MIN, minCorner.asLong());
        tag.putLong(TAG_MAX, maxCorner.asLong());
    tag.putInt(TAG_FACING, facing.get3DDataValue());
    tag.putInt(TAG_BASE_FLOOR_Y, baseFloorY);
        tag.putIntArray(TAG_FLOORS, Objects.requireNonNull(floorYs.stream().mapToInt(Integer::intValue).toArray()));

        ListTag floorMapTag = new ListTag();
        yToLogicalFloor.forEach((y, logical) ->
        {
            CompoundTag entry = new CompoundTag();
            entry.putInt(TAG_FLOOR_Y, y);
            entry.putInt(TAG_FLOOR_LOGICAL, logical);
            floorMapTag.add(entry);
        });
        tag.put(TAG_FLOOR_MAP, Objects.requireNonNull(floorMapTag));
        return tag;
    }

    public static @Nonnull ElevatorMultiblockInstance load(@Nonnull CompoundTag tag)
    {
        BlockPos controller = BlockPos.of(tag.getLong(TAG_CONTROLLER));
        BlockPos inner = BlockPos.of(tag.getLong(TAG_INNER_ORIGIN));
        BlockPos min = BlockPos.of(tag.getLong(TAG_MIN));
        BlockPos max = BlockPos.of(tag.getLong(TAG_MAX));
    Direction facing = Direction.from3DDataValue(tag.getInt(TAG_FACING));

    int[] floorArray = tag.getIntArray(TAG_FLOORS);
    List<Integer> floors = Collections.unmodifiableList(Arrays.stream(floorArray).boxed().toList());
    int baseFloorY = tag.contains(TAG_BASE_FLOOR_Y, Tag.TAG_INT)
        ? tag.getInt(TAG_BASE_FLOOR_Y)
        : (!floors.isEmpty() ? floors.get(0) : inner.getY());

        Map<Integer, Integer> floorMap = new HashMap<>();
    ListTag mapTag = tag.getList(TAG_FLOOR_MAP, Tag.TAG_COMPOUND);
        for (int i = 0; i < mapTag.size(); i++)
        {
            CompoundTag entry = mapTag.getCompound(i);
            floorMap.put(entry.getInt(TAG_FLOOR_Y), entry.getInt(TAG_FLOOR_LOGICAL));
        }

    return new ElevatorMultiblockInstance(
        Objects.requireNonNull(controller),
        Objects.requireNonNull(inner),
        Objects.requireNonNull(min),
        Objects.requireNonNull(max),
        Objects.requireNonNull(facing),
        Objects.requireNonNull(floors),
        Objects.requireNonNull(floorMap),
        baseFloorY);
    }
}
