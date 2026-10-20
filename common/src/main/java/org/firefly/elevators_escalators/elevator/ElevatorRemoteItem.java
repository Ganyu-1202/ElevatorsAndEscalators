package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

/** Server-validated binding, floor registration, selection and requests. */
public final class ElevatorRemoteItem extends Item {
    public ElevatorRemoteItem(Properties properties) { super(properties); }
    public void bind(ItemStack stack, Player player, BlockPos pos) {
        if (player==null || player.isSpectator()) return;
        CompoundTag tag=new CompoundTag(); tag.putLong("Controller",pos.asLong());
        tag.putString("Dimension",player.level().dimension().location().toString()); tag.putInt("SelectedFloor",0);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        ElevatorControllerBlockEntity.tell(player,"bound_multi");
        if (player.level().getBlockEntity(pos) instanceof ElevatorControllerBlockEntity be) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.elevators_escalators.floor_list",java.util.Arrays.toString(be.floors())),false);
        }
    }
    private ElevatorControllerBlockEntity resolve(ItemStack stack, Player player) {
        CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (!tag.contains("Controller") || !player.level().dimension().location().toString().equals(tag.getString("Dimension"))) {
            ElevatorControllerBlockEntity.tell(player,"bind_first"); return null;
        }
        BlockPos pos=BlockPos.of(tag.getLong("Controller"));
        if (player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>64*64) {
            ElevatorControllerBlockEntity.tell(player,"out_of_range"); return null;
        }
        if (!player.level().hasChunkAt(pos) || !(player.level().getBlockEntity(pos) instanceof ElevatorControllerBlockEntity be)) {
            ElevatorControllerBlockEntity.tell(player,"unavailable"); return null;
        }
        return be;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player=context.getPlayer();
        if (player==null || player.isSpectator()) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ElevatorControllerBlockEntity) {
            if (player.isShiftKeyDown()) bind(context.getItemInHand(),player,context.getClickedPos());
            else {
                var controller=(ElevatorControllerBlockEntity)context.getLevel().getBlockEntity(context.getClickedPos());
                if(controller.platform()!=null) { bind(context.getItemInHand(),player,context.getClickedPos()); ElevatorMenu.open(player,controller.platform()); }
                else bind(context.getItemInHand(),player,context.getClickedPos());
            }
        } else {
            var be=resolve(context.getItemInHand(),player);
            if (be!=null) {
                int offset=context.getClickedPos().getY()-be.getBlockPos().getY();
                // Adjacent landing reference block is at controllerY + floor offset.
                if (player.isShiftKeyDown()) be.addFloor(offset,player);
                else {
                    if(be.platform()!=null) ElevatorMenu.open(player,be.platform());
                    else ElevatorControllerBlockEntity.tell(player,"unavailable");
                }
            }
        }
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if (level instanceof ServerLevel && !player.isSpectator()) {
            var be=resolve(stack,player);
            if (be!=null) {
                if(be.platform()!=null) ElevatorMenu.open(player,be.platform());
                else ElevatorControllerBlockEntity.tell(player,"unavailable");
            }
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}
