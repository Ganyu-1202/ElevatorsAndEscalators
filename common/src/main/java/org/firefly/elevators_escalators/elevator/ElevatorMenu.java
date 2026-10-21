package org.firefly.elevators_escalators.elevator;

import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.firefly.elevators_escalators.ModContent;

public final class ElevatorMenu extends AbstractContainerMenu {
    private final ElevatorPlatformEntity platform;
    private final int[] floors;
    private final int[] labels;
    private final ContainerData data;
    public ElevatorMenu(int id,Inventory inventory,FriendlyByteBuf buf) {
        this(id,inventory,buf.readVarInt(),buf.readVarIntArray(256),buf.readVarIntArray(256));
    }
    private ElevatorMenu(int id,Inventory inventory,int entityId,int[] floors,int[] labels) {
        super(ModContent.ELEVATOR_MENU.get(),id);
        var entity=inventory.player.level().getEntity(entityId);
        platform=entity instanceof ElevatorPlatformEntity p?p:null;
        this.floors=floors;this.labels=labels;
        data=inventory.player.level().isClientSide ? new SimpleContainerData(3+floors.length) : new ContainerData() {
            public int get(int index) { if(index>=3)return platform!=null&&platform.requested(floors[index-3])?1:0; return platform==null?0:switch(index) { case 0 -> platform.currentFloor(); case 1 -> platform.targetOffset(); default -> platform.doorsClosed()?1:0; }; }
            public void set(int index,int value) {}
            public int getCount() { return 3+floors.length; }
        };
        addDataSlots(data);
    }
    public boolean selected(int index){return index>=0&&index<floors.length&&data.get(index+3)!=0;}
    public int[] labels(){return labels.clone();}
    public int[] floors() { return floors.clone(); }
    public int currentFloor() { return data.get(0); }
    public int target() { return data.get(1); }
    public boolean closed() { return data.get(2)!=0; }
    public static void open(Player player,ElevatorPlatformEntity platform) {
        if (!(player instanceof ServerPlayer server) || player.isSpectator()) return;
        if (!(platform.level().getBlockEntity(platform.controllerPos()) instanceof ElevatorControllerBlockEntity be)) return;
        int[] floors=be.floors();
        MenuRegistry.openExtendedMenu(server,new SimpleMenuProvider((id,inv,p) -> new ElevatorMenu(id,inv,platform.getId(),floors,be.labels()),Component.translatable("screen.elevators_escalators.title")),buf -> {
            buf.writeVarInt(platform.getId()); buf.writeVarIntArray(floors);buf.writeVarIntArray(be.labels());
        });
    }
    @Override public boolean stillValid(Player player) { return platform!=null && platform.isAlive() && !player.isSpectator() && player.level()==platform.level() && platform.getBoundingBox().expandTowards(0,platform.cabinSpec().cabinHeight(),0).inflate(64).contains(player.position()); }
    @Override public boolean clickMenuButton(Player player,int button) {
        if (!stillValid(player) || player.level().isClientSide) return false;
        if (button>=0 && button<floors.length) { platform.requestFloor(player,floors[button]); return true; }
        if (button==1000 || button==1001) return platform.manualDoor(player,button==1001);
        return false;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) { return ItemStack.EMPTY; }
}
