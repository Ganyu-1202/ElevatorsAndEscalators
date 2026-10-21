package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.ModContent;
import java.util.UUID;
public final class EscalatorStepBlockEntity extends BlockEntity {
 private BlockPos controller=BlockPos.ZERO;private UUID owner;
 public EscalatorStepBlockEntity(BlockPos p,BlockState s){super(ModContent.ESC_STEP_BE.get(),p,s);}
 public void bind(BlockPos p,UUID id){controller=p.immutable();owner=id;setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public boolean owned(BlockPos p,UUID id){return p.equals(controller)&&id.equals(owner);}
 public EscalatorControllerBlockEntity controller(){return level.hasChunkAt(controller)&&level.getBlockEntity(controller)instanceof EscalatorControllerBlockEntity be&&be.id().equals(owner)?be:null;}
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putLong("Controller",controller.asLong());if(owner!=null)t.putUUID("Owner",owner);}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);controller=BlockPos.of(t.getLong("Controller"));owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
