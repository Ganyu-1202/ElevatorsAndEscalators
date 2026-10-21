package org.firefly.elevators_escalators.elevator;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record SetupPayload(int menuId,String fields) implements CustomPacketPayload {
    public static final Type<SetupPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("elevators_escalators","setup"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SetupPayload> CODEC=StreamCodec.of((buf,p)->{buf.writeVarInt(p.menuId);buf.writeUtf(p.fields,100);},buf->new SetupPayload(buf.readVarInt(),buf.readUtf(100)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void register() { NetworkManager.registerReceiver(NetworkManager.c2s(),TYPE,CODEC,(payload,context)->context.queue(()->{
        var player=context.getPlayer();
        if(player.containerMenu instanceof SetupMenu menu && menu.containerId==payload.menuId) menu.submit(payload.fields);
    })); }
}
