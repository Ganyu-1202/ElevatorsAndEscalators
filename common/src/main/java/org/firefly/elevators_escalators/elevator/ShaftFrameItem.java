package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.firefly.elevators_escalators.ModContent;
import java.util.ArrayList;
import java.util.Comparator;

public final class ShaftFrameItem extends BlockItem {
    public ShaftFrameItem(Block block,Properties properties) { super(block,properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ElevatorControllerBlockEntity controller)) return super.useOn(context);
        var player=context.getPlayer();
        if(player==null || player.isSpectator() || !player.mayBuild()) return InteractionResult.FAIL;
        if(context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        ServerLevel level=(ServerLevel)context.getLevel();
        var positions=new ArrayList<BlockPos>();
        for(var cell:ShaftLayout.build(controller.height(),controller.floors())) {
            BlockPos pos=context.getClickedPos().offset(cell.x(),cell.y(),cell.z());
            if(level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos) || !level.hasChunkAt(pos)
                    || !level.mayInteract(player,pos) || !player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())) {
                player.displayClientMessage(Component.translatable("message.elevators_escalators.shaft_invalid"),false); return InteractionResult.FAIL;
            }
            var state=level.getBlockState(pos);
            if(state.is(ModContent.FRAME.get())) continue;
            // Air only. Do not replace water, plants, inventories or existing buildings.
            if(!state.isAir() || level.getBlockEntity(pos)!=null || !level.getEntities((net.minecraft.world.entity.Entity)null,new net.minecraft.world.phys.AABB(pos),e->!e.isSpectator()).isEmpty()) {
                player.displayClientMessage(Component.translatable("message.elevators_escalators.shaft_obstructed",pos.toShortString()),false); return InteractionResult.FAIL;
            }
            positions.add(pos);
        }
        if(positions.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.elevators_escalators.shaft_exists"),true); return InteractionResult.SUCCESS;
        }
        int available=0;
        for(ItemStack stack:player.getInventory().items) if(stack.is(this)) available+=stack.getCount();
        if(player.getOffhandItem().is(this)) available+=player.getOffhandItem().getCount();
        if(!player.getAbilities().instabuild && available<positions.size()) {
            player.displayClientMessage(Component.translatable("message.elevators_escalators.shaft_materials",positions.size(),available),false); return InteractionResult.FAIL;
        }
        positions.sort(Comparator.comparingInt(BlockPos::getY));
        int placed=0;
        for(BlockPos pos:positions) if(level.getBlockState(pos).isAir() && level.setBlock(pos,ModContent.FRAME.get().defaultBlockState(),3)) placed++;
        if(!player.getAbilities().instabuild) {
            int remaining=placed;
            for(ItemStack stack:player.getInventory().items) if(stack.is(this) && remaining>0) { int count=Math.min(stack.getCount(),remaining); stack.shrink(count); remaining-=count; }
            if(remaining>0 && player.getOffhandItem().is(this)) player.getOffhandItem().shrink(remaining);
        }
        player.displayClientMessage(Component.translatable("message.elevators_escalators.shaft_built",placed,positions.size()),false);
        return InteractionResult.CONSUME;
    }
}
