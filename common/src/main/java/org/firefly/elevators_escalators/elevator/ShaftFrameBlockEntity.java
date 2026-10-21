package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.ModContent;
import java.util.UUID;

public final class ShaftFrameBlockEntity extends BlockEntity {
    private BlockPos controller;
    private UUID owner;
    public ShaftFrameBlockEntity(BlockPos pos,BlockState state) { super(ModContent.FRAME_BE.get(),pos,state); }
    public void bind(BlockPos pos,UUID id) { controller=pos.immutable(); owner=id; setChanged(); }
    public boolean ownedBy(BlockPos pos,UUID id) { return pos.equals(controller) && id.equals(owner); }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        if(controller!=null && owner!=null) { tag.putLong("Controller",controller.asLong()); tag.putUUID("Owner",owner); }
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);
        if(tag.hasUUID("Owner")) { controller=BlockPos.of(tag.getLong("Controller")); owner=tag.getUUID("Owner"); }
    }
}
