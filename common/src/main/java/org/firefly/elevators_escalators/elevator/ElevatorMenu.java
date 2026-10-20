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
    private final ContainerData data;
    public ElevatorMenu(int id,Inventory inventory,FriendlyByteBuf buf) {
        this(id,inventory,buf.readVarInt(),buf.readVarIntArray(16));
    }
    private ElevatorMenu(int id,Inventory inventory,int entityId,int[] floors) {
        super(ModContent.ELEVATOR_MENU.get(),id);
        var entity=inventory.player.level().getEntity(entityId);
        platform=entity instanceof ElevatorPlatformEntity p?p:null;
        this.floors=floors;
        data=inventory.player.level().isClientSide ? new SimpleContainerData(3) : new ContainerData() {
            public int get(int index) { return platform==null?0:switch(index) { case 0 -> platform.currentFloor(); case 1 -> platform.targetOffset(); default -> platform.doorsClosed()?1:0; }; }
            public void set(int index,int value) {}
            public int getCount() { return 3; }
        };
        addDataSlots(data);
    }
    public int[] floors() { return floors.clone(); }
    public int currentFloor() { return data.get(0); }
    public int target() { return data.get(1); }
    public boolean closed() { return data.get(2)!=0; }
    public static void open(Player player,ElevatorPlatformEntity platform) {
        if (!(player instanceof ServerPlayer server) || player.isSpectator()) return;
        if (!(platform.level().getBlockEntity(platform.controllerPos()) instanceof ElevatorControllerBlockEntity be)) return;
        int[] floors=be.floors();
        MenuRegistry.openExtendedMenu(server,new SimpleMenuProvider((id,inv,p) -> new ElevatorMenu(id,inv,platform.getId(),floors),Component.translatable("screen.elevators_escalators.title")),buf -> {
            buf.writeVarInt(platform.getId()); buf.writeVarIntArray(floors);
        });
    }
    @Override public boolean stillValid(Player player) { return platform!=null && platform.isAlive() && !player.isSpectator() && player.level()==platform.level() && player.distanceToSqr(platform)<=64*64; }
    @Override public boolean clickMenuButton(Player player,int button) {
        if (!stillValid(player) || player.level().isClientSide) return false;
        if (button>=0 && button<floors.length) { platform.requestFloor(player,floors[button]); return true; }
        if (button==100 || button==101) return platform.manualDoor(player,button==101);
        return false;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) { return ItemStack.EMPTY; }
}
