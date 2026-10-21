package org.firefly.elevators_escalators.escalator;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.firefly.elevators_escalators.ModContent;
public final class EscalatorMenu extends AbstractContainerMenu {
 private final Player player;private final BlockPos pos;private final ContainerData data;
 public EscalatorMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,buf.readBlockPos());}
 private EscalatorMenu(int id,Inventory inv,BlockPos pos){super(ModContent.ESC_MENU.get(),id);this.player=inv.player;this.pos=pos;
  data=player.level().isClientSide?new SimpleContainerData(4):new ContainerData(){public int get(int i){var b=controller();return b==null?0:switch(i){case 0->b.built()?1:0;case 1->b.speed();case 2->b.mode();default->b.hasEnd()?1:0;};}public void set(int i,int v){}public int getCount(){return 4;}};addDataSlots(data);
 }
 private EscalatorControllerBlockEntity controller(){return player.level().hasChunkAt(pos)&&player.level().getBlockEntity(pos)instanceof EscalatorControllerBlockEntity be?be:null;}
 public boolean built(){return data.get(0)!=0;}public int speed(){return data.get(1);}public int mode(){return data.get(2);}public boolean hasEnd(){return data.get(3)!=0;}
 public static void open(Player player,EscalatorControllerBlockEntity be){if(!(player instanceof ServerPlayer sp)||player.isSpectator())return;MenuRegistry.openExtendedMenu(sp,new SimpleMenuProvider((id,inv,p)->new EscalatorMenu(id,inv,be.getBlockPos()),Component.literal("自动扶梯 / Escalator")),b->b.writeBlockPos(be.getBlockPos()));}
 public void action(int action,int speed){if(stillValid(player)&&!player.level().isClientSide&&action>=0&&action<=3)controller().action(player,action,speed);}
 @Override public boolean stillValid(Player p){return !p.isSpectator()&&p.distanceToSqr(pos.getX(),pos.getY(),pos.getZ())<=520*520&&controller()!=null;}
 @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
}
