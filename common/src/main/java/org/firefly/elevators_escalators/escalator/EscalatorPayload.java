package org.firefly.elevators_escalators.escalator;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record EscalatorPayload(int menu,int action,int speed)implements CustomPacketPayload{
 public static final Type<EscalatorPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("elevators_escalators","escalator_control"));
 public static final StreamCodec<RegistryFriendlyByteBuf,EscalatorPayload> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.menu);b.writeVarInt(p.action);b.writeVarInt(p.speed);},b->new EscalatorPayload(b.readVarInt(),b.readVarInt(),b.readVarInt()));
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void register(){NetworkManager.registerReceiver(NetworkManager.c2s(),TYPE,CODEC,(p,c)->c.queue(()->{if(c.getPlayer().containerMenu instanceof EscalatorMenu m&&m.containerId==p.menu)m.action(p.action,p.speed);}));}
}
