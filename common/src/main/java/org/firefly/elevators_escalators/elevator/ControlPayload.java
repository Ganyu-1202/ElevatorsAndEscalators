package org.firefly.elevators_escalators.elevator;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record ControlPayload(int menuId,int button) implements CustomPacketPayload {
    public static final Type<ControlPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("elevators_escalators","control"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ControlPayload> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.menuId);b.writeVarInt(p.button);},b->new ControlPayload(b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(){NetworkManager.registerReceiver(NetworkManager.c2s(),TYPE,CODEC,(p,c)->c.queue(()->{var player=c.getPlayer();if(player.containerMenu instanceof ElevatorMenu m && m.containerId==p.menuId)m.clickMenuButton(player,p.button);}));}
}
