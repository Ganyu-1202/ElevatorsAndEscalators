package org.firefly.elevators_escalators.elevator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.ModContent;
import java.util.UUID;
public final class HallPanelBlockEntity extends BlockEntity {
    private BlockPos controller=BlockPos.ZERO;private UUID cabin;private int index;
    public HallPanelBlockEntity(BlockPos p,BlockState s){super(ModContent.HALL_BE.get(),p,s);}
    public void bind(BlockPos p,UUID cabin,int index){controller=p.immutable();this.cabin=cabin;this.index=index;setChanged();}
    public boolean belongs(BlockPos p,UUID id){return p.equals(controller)&&id!=null&&id.equals(cabin);}
    public void call(Player player,boolean up){
        if(player.isSpectator()||!level.hasChunkAt(controller))return;
        if(!(level.getBlockEntity(controller) instanceof ElevatorControllerBlockEntity c)||!c.owns(cabin)||c.platform()==null){player.displayClientMessage(Component.literal("电梯未加载或已拆除 / Elevator unavailable"),true);return;}
        int[] floors=c.floors();if(index<0||index>=floors.length)return;
        if(up&&index==floors.length-1||!up&&index==0){player.displayClientMessage(Component.literal("该层没有此方向 / No floor in this direction"),true);return;}
        c.platform().requestHall(player,floors[index],up?1:-1);
    }
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putLong("Controller",controller.asLong());t.putInt("Index",index);if(cabin!=null)t.putUUID("Cabin",cabin);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);controller=BlockPos.of(t.getLong("Controller"));index=t.getInt("Index");cabin=t.hasUUID("Cabin")?t.getUUID("Cabin"):null;}
}
