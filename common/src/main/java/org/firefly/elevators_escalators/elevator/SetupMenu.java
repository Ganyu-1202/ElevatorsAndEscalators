package org.firefly.elevators_escalators.elevator;

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

public final class SetupMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final int minSize,maxSize,maxFloors;
    private final Player player;
    public SetupMenu(int id,Inventory inventory,FriendlyByteBuf buf) { this(id,inventory,buf.readBlockPos(),buf.readVarInt(),buf.readVarInt(),buf.readVarInt()); }
    private SetupMenu(int id,Inventory inventory,BlockPos pos,int min,int max,int floors) {
        super(ModContent.SETUP_MENU.get(),id); player=inventory.player; this.pos=pos;minSize=min;maxSize=max;maxFloors=floors;
    }
    public static void open(Player player,BlockPos pos) {
        if(!(player instanceof ServerPlayer server)) return;
        var l=ElevatorLimits.INSTANCE;
        MenuRegistry.openExtendedMenu(server,new SimpleMenuProvider((id,inv,p)->new SetupMenu(id,inv,pos,l.minSize,l.maxSize,l.maxFloors),Component.literal("电梯配置 / Elevator Setup")),buf->{buf.writeBlockPos(pos);buf.writeVarInt(l.minSize);buf.writeVarInt(l.maxSize);buf.writeVarInt(l.maxFloors);});
    }
    public void submit(String text) {
        if(!stillValid(player) || player.level().isClientSide || text.length()>100) return;
        try {
            String[] fields=text.split(",",-1); if(fields.length!=5) throw new IllegalArgumentException();
            int[] values=java.util.Arrays.stream(fields).mapToInt(Integer::parseInt).toArray();
            if(player.level().getBlockEntity(pos) instanceof ElevatorControllerBlockEntity controller)
                controller.configure(player,new ElevatorSpec(values[0],values[1],values[2],values[3],values[4],values[2]));
        } catch(IllegalArgumentException ex) { player.displayClientMessage(Component.literal("请输入五个合法整数 / Enter five valid integers"),false); }
    }
    @Override public boolean stillValid(Player p) { return !p.isSpectator() && p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64 && p.level().getBlockEntity(pos) instanceof ElevatorControllerBlockEntity c && !c.configured(); }
    @Override public ItemStack quickMoveStack(Player p,int i) { return ItemStack.EMPTY; }
}
