package org.firefly.elevators_escalators.elevator.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.firefly.elevators_escalators.ElevatorsEscalators;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ElevatorMultiblockSavedData extends SavedData
{
    private static final String DATA_NAME = ElevatorsEscalators.MODID + "_multiblocks";
    private static final String TAG_INSTANCES = "instances";
    private static final String TAG_ENTRY_INSTANCE = "instance";
    private static final String TAG_ENTRY_STATE = "state";
    private static final SavedData.Factory<ElevatorMultiblockSavedData> FACTORY =
            new SavedData.Factory<>(ElevatorMultiblockSavedData::new, ElevatorMultiblockSavedData::load);

    private final Map<BlockPos, ElevatorMultiblockInstance> instancesByController = new HashMap<>();
    private final Map<BlockPos, CarriageRuntimeState> runtimeStatesByController = new HashMap<>();

    public ElevatorMultiblockSavedData()
    {
    }

    public static @Nonnull ElevatorMultiblockSavedData get(@Nonnull ServerLevel level)
    {
    return Objects.requireNonNull(level.getDataStorage().computeIfAbsent(Objects.requireNonNull(FACTORY), DATA_NAME));
    }

    private static @Nonnull ElevatorMultiblockSavedData load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider)
    {
        ElevatorMultiblockSavedData data = new ElevatorMultiblockSavedData();
        ListTag list = tag.getList(TAG_INSTANCES, net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag entry = list.getCompound(i);
            CompoundTag instanceTag;
            CompoundTag stateTag = null;
            if (entry.contains(TAG_ENTRY_INSTANCE, net.minecraft.nbt.Tag.TAG_COMPOUND))
            {
                instanceTag = entry.getCompound(TAG_ENTRY_INSTANCE);
                if (entry.contains(TAG_ENTRY_STATE, net.minecraft.nbt.Tag.TAG_COMPOUND))
                {
                    stateTag = entry.getCompound(TAG_ENTRY_STATE);
                }
            }
            else
            {
                instanceTag = entry;
            }

            ElevatorMultiblockInstance instance = ElevatorMultiblockInstance.load(Objects.requireNonNull(instanceTag));
            data.instancesByController.put(instance.controllerPos(), instance);

            CarriageRuntimeState runtimeState = stateTag != null
                    ? CarriageRuntimeState.load(stateTag)
                    : CarriageRuntimeState.blocks(instance.baseFloorY());
            data.runtimeStatesByController.put(instance.controllerPos(), runtimeState);
        }
        return data;
    }

    @Override
    public @Nonnull CompoundTag save(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider)
    {
        ListTag list = new ListTag();
        for (ElevatorMultiblockInstance instance : instancesByController.values())
        {
            CompoundTag entry = new CompoundTag();
            entry.put(TAG_ENTRY_INSTANCE, instance.save());
            CarriageRuntimeState state = runtimeStatesByController.get(instance.controllerPos());
            if (state != null)
            {
                entry.put(TAG_ENTRY_STATE, state.save());
            }
            list.add(entry);
        }
        tag.put(TAG_INSTANCES, list);
        return tag;
    }

    public void add(@Nonnull ElevatorMultiblockInstance instance)
    {
        instancesByController.put(instance.controllerPos(), instance);
        runtimeStatesByController.putIfAbsent(instance.controllerPos(), CarriageRuntimeState.blocks(instance.baseFloorY()));
        this.setDirty();
    }

    public @Nonnull Optional<ElevatorMultiblockInstance> getByController(@Nonnull BlockPos controllerPos)
    {
        return Objects.requireNonNull(Optional.ofNullable(instancesByController.get(controllerPos)));
    }

    public @Nonnull Optional<CarriageRuntimeState> getRuntimeState(@Nonnull BlockPos controllerPos)
    {
        return Objects.requireNonNull(Optional.ofNullable(runtimeStatesByController.get(controllerPos)));
    }

    public void setRuntimeState(@Nonnull BlockPos controllerPos, @Nonnull CarriageRuntimeState state)
    {
        runtimeStatesByController.put(controllerPos, state);
        this.setDirty();
    }

    public @Nonnull Optional<ElevatorMultiblockInstance> findContaining(@Nonnull BlockPos pos)
    {
        Objects.requireNonNull(pos);
        return Objects.requireNonNull(instancesByController.values().stream()
                .filter(instance -> instance.contains(pos))
                .findFirst());
    }

    public static final class CarriageRuntimeState
    {
        private static final String TAG_RESTING_Y = "restingY";
        private static final String TAG_AS_BLOCKS = "asBlocks";

        private final int restingFloorY;
        private final boolean representedAsBlocks;

        private CarriageRuntimeState(int restingFloorY, boolean representedAsBlocks)
        {
            this.restingFloorY = restingFloorY;
            this.representedAsBlocks = representedAsBlocks;
        }

        public static @Nonnull CarriageRuntimeState blocks(int restingFloorY)
        {
            return new CarriageRuntimeState(restingFloorY, true);
        }

        public static @Nonnull CarriageRuntimeState entityActive(int restingFloorY)
        {
            return new CarriageRuntimeState(restingFloorY, false);
        }

        public int restingFloorY()
        {
            return restingFloorY;
        }

        public boolean isRepresentedAsBlocks()
        {
            return representedAsBlocks;
        }

        public @Nonnull CompoundTag save()
        {
            CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_RESTING_Y, restingFloorY);
            tag.putBoolean(TAG_AS_BLOCKS, representedAsBlocks);
            return tag;
        }

        public static @Nonnull CarriageRuntimeState load(@Nonnull CompoundTag tag)
        {
            int resting = tag.getInt(TAG_RESTING_Y);
            boolean asBlocks = tag.getBoolean(TAG_AS_BLOCKS);
            return new CarriageRuntimeState(resting, asBlocks);
        }

        public @Nonnull CarriageRuntimeState withRestingFloor(int newFloor)
        {
            return new CarriageRuntimeState(newFloor, representedAsBlocks);
        }

        public @Nonnull CarriageRuntimeState withRepresentation(boolean asBlocks)
        {
            return new CarriageRuntimeState(restingFloorY, asBlocks);
        }
    }
}
